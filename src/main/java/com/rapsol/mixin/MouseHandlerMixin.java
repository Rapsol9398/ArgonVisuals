package com.rapsol.mixin;

import com.rapsol.event.EventManager;
import com.rapsol.event.events.ButtonListener;
import com.rapsol.module.modules.render.Zoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "onButton", at = @At("HEAD"))
	private void onMousePress(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		EventManager.fire(new ButtonListener.ButtonEvent(buttonInfo.button(), window, action));
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void onMouseScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (minecraft.screen == null && Zoom.isActive() && Zoom.instance.onScroll(yOffset))
			ci.cancel();
	}

	@ModifyArgs(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void onTurn(Args args) {
		if (Zoom.instance == null) return;
		double scale = Zoom.instance.sensitivity();
		args.set(0, (double) args.get(0) * scale);
		args.set(1, (double) args.get(1) * scale);
	}
}
