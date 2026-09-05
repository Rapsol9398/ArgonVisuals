package com.rapsol.mixin;

import com.rapsol.event.EventManager;
import com.rapsol.event.events.ButtonListener;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void onPress(long window, int action, KeyEvent event, CallbackInfo ci) {
		EventManager.fire(new ButtonListener.ButtonEvent(event.key(), window, action));
	}
}
