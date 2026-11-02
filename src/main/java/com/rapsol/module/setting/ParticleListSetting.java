package com.rapsol.module.setting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ParticleListSetting extends Setting<ParticleListSetting> {
	private final Set<String> hidden = new HashSet<>();

	public ParticleListSetting(CharSequence name) {
		super(name);
	}

	public boolean isHidden(String id) {
		return hidden.contains(id);
	}

	public void setHidden(String id, boolean hide) {
		if (hide) hidden.add(id);
		else hidden.remove(id);
	}

	public Set<String> hidden() {
		return Set.copyOf(hidden);
	}

	public void replaceAll(Collection<String> ids) {
		hidden.clear();
		hidden.addAll(ids);
	}
}
