package com.rapsol.event;

public abstract class CancellableEvent<T extends Listener> extends Event<T> {
    private boolean cancelled;

    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }
}