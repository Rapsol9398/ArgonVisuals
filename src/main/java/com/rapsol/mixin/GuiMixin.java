package com.rapsol.mixin;

import com.rapsol.Argon;
import com.rapsol.event.EventManager;
import com.rapsol.event.events.HudListener;
import com.rapsol.module.modules.render.ArmorHud;
import com.rapsol.module.modules.render.NoRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRenderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        HudListener.HudEvent event = new HudListener.HudEvent(context, tickCounter.getGameTimeDeltaPartialTick(true));

        EventManager.fire(event);
    }
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void argon$renderArmorHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        ArmorHud module = Argon.INSTANCE.getModuleManager().getModule(ArmorHud.class);
        if (module != null) {
            module.onHudRender(context);
        }
    }

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onExtractScoreboardSidebar(GuiGraphicsExtractor graphics, Objective objective, CallbackInfo ci) {
        NoRender norender = Argon.INSTANCE.getModuleManager().getModule(NoRender.class);
        if (norender.isEnabled() && norender.scoreboard.getValue()) ci.cancel();
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onExtractScoreboardSidebar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        NoRender norender = Argon.INSTANCE.getModuleManager().getModule(NoRender.class);
        if (norender.isEnabled() && norender.scoreboard.getValue()) ci.cancel();
    }

    @ModifyArgs(method = "extractCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V", ordinal = 0))
    private void onExtractPumpkinOverlay(Args args) {
        NoRender norender = Argon.INSTANCE.getModuleManager().getModule(NoRender.class);
        if (norender.isEnabled() && norender.pumpkinOverlay.getValue()) args.set(2, 0f);
    }
}
