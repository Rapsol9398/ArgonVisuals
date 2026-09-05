package com.rapsol;

import com.rapsol.event.EventManager;
import com.rapsol.gui.ClickGui;
import com.rapsol.managers.ProfileManager;
import com.rapsol.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;

@SuppressWarnings("all")
public final class Argon {
	public ProfileManager profileManager;
	public ModuleManager moduleManager;
	public EventManager eventManager;
	public static Minecraft mc;
	public String version = " b1.3";
	public static Argon INSTANCE;
	public ClickGui clickGui;
	public Screen previousScreen = null;

	public Argon() throws InterruptedException, IOException {
		INSTANCE = this;
		this.eventManager = new EventManager();
		this.moduleManager = new ModuleManager();
		this.clickGui = new ClickGui();
		this.profileManager = new ProfileManager();
		this.getProfileManager().loadProfile();
		mc = Minecraft.getInstance();
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}
	public ModuleManager getModuleManager() {
		return moduleManager;
	}
	public EventManager getEventManager() {
		return eventManager;
	}
	public ClickGui getClickGui() {
		return clickGui;
	}
	public String getVersion() {
		return version;
	}
}
