package com.rapsol.managers;

import com.rapsol.Argon;
import com.rapsol.module.Module;
import com.rapsol.module.setting.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ProfileManager {
	private static final String fileName = "argonvisuals.json";

	private final Gson g = new Gson();
	private final Path profilePath;
	private final Path tempPath;
	private JsonObject profile;

	private final AtomicBoolean dirty = new AtomicBoolean(false);

	public ProfileManager() {
		profilePath = FabricLoader.getInstance().getConfigDir().resolve(fileName);
		tempPath = FabricLoader.getInstance().getConfigDir().resolve(fileName + ".tmp");
	}

	public void markDirty() {
		dirty.set(true);
	}

	public void tick() {
		if (dirty.compareAndSet(true, false)) {
			saveProfile();
		}
	}

	public void loadProfile() {
		try {
			if (!Files.isRegularFile(profilePath))
				return;

			profile = g.fromJson(Files.readString(profilePath), JsonObject.class);
			if (profile == null)
				return;

			for (Module module : Argon.INSTANCE.getModuleManager().getModules()) {
				JsonElement moduleJson = profile.get(String.valueOf(Argon.INSTANCE.getModuleManager().getModules().indexOf(module)));
				if (moduleJson == null || !moduleJson.isJsonObject())
					continue;
				JsonObject moduleConfig = moduleJson.getAsJsonObject();

				JsonElement enabledJson = moduleConfig.get("enabled");
				if (enabledJson == null || !enabledJson.isJsonPrimitive())
					continue;

				if (enabledJson.getAsBoolean())
					module.setEnabledStatus(true);

				for (Setting<?> setting : module.getSettings()) {
					JsonElement settingJson = moduleConfig.get(String.valueOf(module.getSettings().indexOf(setting)));
					if (settingJson == null)
						continue;

					if (setting instanceof BooleanSetting booleanSetting) {
						booleanSetting.setValue(settingJson.getAsBoolean());
					} else if (setting instanceof ModeSetting<?> modeSetting) {
						modeSetting.setModeIndex(settingJson.getAsInt());
					} else if (setting instanceof NumberSetting numberSetting) {
						numberSetting.setValue(settingJson.getAsDouble());
					} else if (setting instanceof KeybindSetting keybindSetting) {
						keybindSetting.setKey(settingJson.getAsInt());
						if (keybindSetting.isModuleKey())
							module.setKey(settingJson.getAsInt());
					} else if (setting instanceof StringSetting stringSetting) {
						stringSetting.setValue(settingJson.getAsString());
					} else if (setting instanceof ColorSetting colorSetting) {
						colorSetting.setValue(new Color(settingJson.getAsInt(), true));
					} else if (setting instanceof MinMaxSetting minMaxSetting) {
						if (settingJson.isJsonObject()) {
							JsonObject minMaxObject = settingJson.getAsJsonObject();
							double minValue = minMaxObject.get("1").getAsDouble();
							double maxValue = minMaxObject.get("2").getAsDouble();

							minMaxSetting.setMinValue(minValue);
							minMaxSetting.setMaxValue(maxValue);
						}
					}
				}

				if (enabledJson.getAsBoolean()) {
					module.onEnable();
				}
			}

			JsonElement hudElement = profile.get("hud");
			if (hudElement != null && hudElement.isJsonObject()) {
				JsonObject hudJson = hudElement.getAsJsonObject();
				for (com.rapsol.gui.HudElement element : com.rapsol.gui.HudElement.values()) {
					JsonElement pointJson = hudJson.get(element.name());
					if (pointJson == null || !pointJson.isJsonObject()) continue;
					JsonObject point = pointJson.getAsJsonObject();

					com.rapsol.gui.HudPoint p = com.rapsol.gui.HudLayout.get(element);
					p.anchorX = point.has("anchorX")
							? com.rapsol.gui.HudPoint.Anchor.valueOf(point.get("anchorX").getAsString())
							: com.rapsol.gui.HudPoint.Anchor.START;
					p.anchorY = point.has("anchorY")
							? com.rapsol.gui.HudPoint.Anchor.valueOf(point.get("anchorY").getAsString())
							: com.rapsol.gui.HudPoint.Anchor.START;
					p.offsetX = point.has("offsetX") ? point.get("offsetX").getAsDouble() : element.defaultX;
					p.offsetY = point.has("offsetY") ? point.get("offsetY").getAsDouble() : element.defaultY;
					p.scale = point.has("scale") ? point.get("scale").getAsDouble() : element.defaultScale;
				}
			}
		} catch (Exception ignored) {
		}
	}

	public synchronized void saveProfile() {
		try {
			Files.createDirectories(profilePath.getParent());
			profile = new JsonObject();

			for (Module module : Argon.INSTANCE.getModuleManager().getModules()) {
				JsonObject moduleConfig = new JsonObject();

				boolean isGuiModule = "ArgonVisuals".contentEquals(module.getName());
				moduleConfig.addProperty("enabled", isGuiModule ? false : module.isEnabled()); //do NOT remove this!!

				for (Setting<?> setting : module.getSettings()) {
					if (setting instanceof BooleanSetting booleanSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), booleanSetting.getValue());
					} else if (setting instanceof ModeSetting<?> modeSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), modeSetting.getModeIndex());
					} else if (setting instanceof NumberSetting numberSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), numberSetting.getValue());
					} else if (setting instanceof KeybindSetting keybindSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), keybindSetting.getKey());
					} else if (setting instanceof StringSetting stringSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), stringSetting.getValue());
					} else if (setting instanceof ColorSetting colorSetting) {
						moduleConfig.addProperty(String.valueOf(module.getSettings().indexOf(setting)), colorSetting.getValue().getRGB());
					} else if (setting instanceof MinMaxSetting minMaxSetting) {
						JsonObject minMaxObject = new JsonObject();
						minMaxObject.addProperty("1", minMaxSetting.getMinValue());
						minMaxObject.addProperty("2", minMaxSetting.getMaxValue());

						moduleConfig.add(String.valueOf(module.getSettings().indexOf(setting)), minMaxObject);
					}
				}

				profile.add(String.valueOf(Argon.INSTANCE.getModuleManager().getModules().indexOf(module)), moduleConfig);
			}

			JsonObject hudJson = new JsonObject();
			for (var entry : com.rapsol.gui.HudLayout.all().entrySet()) {
				com.rapsol.gui.HudPoint p = entry.getValue();
				JsonObject point = new JsonObject();
				point.addProperty("anchorX", p.anchorX.name());
				point.addProperty("anchorY", p.anchorY.name());
				point.addProperty("offsetX", p.offsetX);
				point.addProperty("offsetY", p.offsetY);
				point.addProperty("scale", p.scale);
				hudJson.add(entry.getKey().name(), point);
			}
			profile.add("hud", hudJson);

			Files.writeString(tempPath, g.toJson(profile));
			try {
				Files.move(tempPath, profilePath,
						StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException e) {
				Files.move(tempPath, profilePath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ignored) {
		}
	}
}