package com.rapsol.event.events;

import com.rapsol.event.CancellableEvent;
import com.rapsol.event.Listener;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;

public interface PacketReceiveListener extends Listener {
    void onPacketReceive(PacketReceiveEvent event);

    class PacketReceiveEvent extends CancellableEvent<PacketReceiveListener> {
        public Packet<?> packet;

        public PacketReceiveEvent(Packet<?> packet) {
            this.packet = packet;
        }

        @Override
        public void fire(ArrayList<PacketReceiveListener> listeners) {
            listeners.forEach(e -> e.onPacketReceive(this));
        }

        @Override
        public Class<PacketReceiveListener> getListenerType() {
            return PacketReceiveListener.class;
        }
    }
}