package com.rapsol.module;

import com.rapsol.Argon;
import com.rapsol.event.events.ButtonListener;
import com.rapsol.module.modules.render.ArmorHud;
import com.rapsol.module.modules.client.ClickGUI;
import com.rapsol.module.modules.client.HUD;
import com.rapsol.module.modules.client.HudEditor;
import com.rapsol.module.modules.render.*;
import com.rapsol.module.setting.KeybindSetting;
import com.rapsol.utils.printString;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager implements ButtonListener {
	private final List<Module> modules = new ArrayList<>();

	public ModuleManager() {
		addModules();
		addKeybinds();
	}

	public void addModules() {
		add(new Sky());
		add(new Fog());
		add(new VoidColor());
		add(new Fullbright());
		add(new Totem());
		add(new ClickGUI());
		add(new HUD());
		add(new Particle());
		add(new Freelook());
		add(new ArmorHud());
		add(new HudEditor());
		add(new ItemPhysics());
		add(new ShulkerTooltip());
		add(new NoRender());
	}

	public List<Module> getEnabledModules() {
		return modules.stream()
				.filter(Module::isEnabled)
				.toList();
	}

	public List<Module> getModules() {
		return modules;
	}

	public void addKeybinds() {
		Argon.INSTANCE.getEventManager().add(ButtonListener.class, this);

		for (Module module : modules) {
			module.addSetting(new KeybindSetting(printString.of("Keybind"), module.getKey(), true)
					.setDescription(printString.of("Key to enabled the module")));
		}
	}

	public List<Module> getModulesInCategory(Category category) {
		return modules.stream()
				.filter(module -> module.getCategory() == category)
				.toList();
	}

	@SuppressWarnings("unchecked")
	public <T extends Module> T getModule(Class<T> moduleClass) {
		return (T) modules.stream()
				.filter(moduleClass::isInstance)
				.findFirst()
				.orElse(null);
	}

	public void add(Module module) {
		modules.add(module);
	}

	@Override
	public void onButtonPress(ButtonEvent event) {
		if (event.button == GLFW.GLFW_KEY_UNKNOWN || event.action != GLFW.GLFW_PRESS) {
			return;
		}

		modules.forEach(module -> {
			if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN && module.getKey() == event.button) {
				module.toggle();
			}
		});
	}
}

