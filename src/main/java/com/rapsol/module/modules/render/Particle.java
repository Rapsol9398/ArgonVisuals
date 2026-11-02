package com.rapsol.module.modules.render;


import com.rapsol.Argon;
import com.rapsol.gui.ParticlePanel;
import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.ActionSetting;
import com.rapsol.module.setting.ParticleListSetting;
import com.rapsol.utils.printString;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class Particle extends Module {
    public final ActionSetting open = new ActionSetting(printString.of("Particles..."), () -> Argon.INSTANCE.clickGui.panel = new ParticlePanel(this))
            .setDescription(printString.of("Choose which particles are shown"));
    public final ParticleListSetting particles = new ParticleListSetting(printString.of("Particles"));

    private List<String> ids;

    public Particle() {
        super(printString.of("Particle"), printString.of("toggle particle rendering"), -1, Category.RENDER);
        addSettings(open, particles);
    }

    public boolean hides(ParticleType<?> type) {
        Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
        return id != null && id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && particles.isHidden(id.getPath());
    }

    public List<String> ids() {
        if (ids == null) {
            ids = new ArrayList<>();
            for (Identifier id : BuiltInRegistries.PARTICLE_TYPE.keySet())
                if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                    ids.add(id.getPath());
        }
        return ids;
    }
}
