package me.earth.phobos.features;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.gui.PhobosGui;
import me.earth.phobos.features.modules.Module;
import me.earth.phobos.features.setting.Setting;
import me.earth.phobos.manager.TextManager;
import me.earth.phobos.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Feature implements Util {

	public List<Setting> settings = new ArrayList<>();
	public TextManager renderer = Phobos.textManager;
	private String name;

	public Feature() {}

	public Feature(String name) { this.name = name; }
	
	public static boolean nullCheck() {
		return mc.player == null;
	}

	public static boolean fullNullCheck() {
		return mc.player == null || mc.world == null;
	}

	public String getName() {
		return this.name;
	}

	public List<Setting> getSettings() {
		return this.settings;
	}

	public boolean hasSettings() {
		return !this.settings.isEmpty();
	}

	public boolean isEnabled() {
		if (this instanceof Module) {
			return ((Module) this).isOn();
		}
		return false;
	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	public Setting register(Setting setting) {
		setting.setFeature(this);
		this.settings.add(setting);
		if(this instanceof Module && mc.currentScreen instanceof PhobosGui) {
			PhobosGui.getInstance().updateModule((Module)this);
		}
		return setting;
	}

	public void unregister(Setting settingIn) {
		List<Setting> removeList = new ArrayList<>();
		for(Setting setting : this.settings) {
			if(setting.equals(settingIn)) {
				removeList.add(setting);
			}
		}

		if(!removeList.isEmpty()) {
			this.settings.removeAll(removeList);
		}

		if(this instanceof Module && mc.currentScreen instanceof PhobosGui) {
			PhobosGui.getInstance().updateModule((Module)this);
		}
	}

	public Setting getSettingByName(String name) {
		for(Setting setting : this.settings) {
			if(setting.getName().equalsIgnoreCase(name)) {
				return setting;
			}
		}
		return null;
	}

	public void reset() {
		for(Setting setting : this.settings) {
			setting.setValue(setting.getDefaultValue());
		}
	}

	public void clearSettings() {
		this.settings = new ArrayList<>();
	}
}
