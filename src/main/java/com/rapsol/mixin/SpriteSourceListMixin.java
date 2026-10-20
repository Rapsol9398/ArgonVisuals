package com.rapsol.mixin;

import com.rapsol.module.modules.render.Fire;
import com.rapsol.module.modules.render.Totem;
import com.rapsol.utils.ColorUtils;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteSourceList.class)
public class SpriteSourceListMixin {

    @Shadow @Final private List<SpriteSource> sources;

    @Inject(method = "load", at = @At("RETURN"))
    private static void onLoad(ResourceManager resourceManager, Identifier atlas, CallbackInfoReturnable<SpriteSourceList> cir) {
        List<SpriteSource> sources = ((SpriteSourceListMixin) (Object) cir.getReturnValue()).sources;
        if (atlas.equals(AtlasIds.BLOCKS))
            sources.add(ColorUtils.graySprites(Fire.TEXTURES));
        if (atlas.equals(AtlasIds.ITEMS))
            sources.add(ColorUtils.graySprites(Totem.TEXTURES));
    }
}
