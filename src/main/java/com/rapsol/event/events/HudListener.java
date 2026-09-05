package com.rapsol.event.events;

import com.rapsol.event.Event;
import com.rapsol.event.Listener;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;

public interface HudListener extends Listener {
    void onRenderHud(HudEvent event);

    class HudEvent extends Event<HudListener> {
        public GuiGraphicsExtractor context;
        public float delta;

        public HudEvent(GuiGraphicsExtractor context, float delta) {
            this.context = context;
            this.delta = delta;
        }

        @Override
        public void fire(ArrayList<HudListener> listeners) {
            listeners.forEach(e -> e.onRenderHud(this));
        }

        @Override
        public Class<HudListener> getListenerType() {
            return HudListener.class;
        }
    }
}