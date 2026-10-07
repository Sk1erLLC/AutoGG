package club.sk1er.mods.autogg.handlers.gg;

import java.util.function.LongSupplier;

/**
 * Lets something happen at most once per period, so two end-of-game lines don't send GG twice.
 */
public final class Cooldown {
    private final long periodMs;
    private final LongSupplier clock;
    private long last = Long.MIN_VALUE;

    public Cooldown(long periodMs, LongSupplier clock) {
        this.periodMs = periodMs;
        this.clock = clock;
    }

    /**
     * @return true, and starts the period, when the last one has passed
     */
    public synchronized boolean tryAcquire() {
        long now = clock.getAsLong();
        if (last != Long.MIN_VALUE && now - last < periodMs) return false;
        last = now;
        return true;
    }
}
