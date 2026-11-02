package com.rapsol.module.modules.render;

import com.rapsol.module.Category;
import com.rapsol.module.Module;
import com.rapsol.module.setting.KeybindSetting;
import com.rapsol.utils.KeyUtils;
import com.rapsol.utils.printString;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

public final class Freelook extends Module {

    public static Freelook instance;
    public float maxYaw = 360.0f;

    public final KeybindSetting holdKey = new KeybindSetting(printString.of("Hold bind"), InputConstants.KEY_LALT, false);

    public Freelook() {
        super(printString.of("Freelook"), printString.of("free third person perspective (hold Left Alt)"), -1, Category.RENDER);
        instance = this;
        addSetting(holdKey);
    }

    @Override
    public void onDisable() {
        Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        super.onDisable();
    }

    public static boolean isFreeLooking() {
        return instance != null && instance.isEnabled() && isHoldKeyDown();
    }

    public static boolean isHoldKeyDown() {
        if (instance.holdKey.getKey() == -1)
            return true;
        return KeyUtils.isKeyPressed(instance.holdKey.getKey());
    }

    public interface CameraOverriddenEntity {
        float freelook$getCameraPitch();
        float freelook$getCameraYaw();
        void freelook$setCameraPitch(float pitch);
        void freelook$setCameraYaw(float yaw);
    }
}