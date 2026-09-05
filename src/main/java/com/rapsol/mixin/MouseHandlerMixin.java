package com.rapsol.mixin;

import com.rapsol.event.EventManager;
import com.rapsol.event.events.ButtonListener;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onButton", at = @At("HEAD"))
	private void onMousePress(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
		EventManager.fire(new ButtonListener.ButtonEvent(buttonInfo.button(), window, action));
	}
}
