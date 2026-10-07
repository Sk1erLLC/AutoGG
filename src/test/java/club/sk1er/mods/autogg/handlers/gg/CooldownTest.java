package club.sk1er.mods.autogg.handlers.gg;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownTest {
    @Test
    void allowsOncePerPeriod() {
        AtomicLong now = new AtomicLong(1_000_000);
        Cooldown cooldown = new Cooldown(10_000, now::get);
        assertTrue(cooldown.tryAcquire());
        now.addAndGet(5_000);
        assertFalse(cooldown.tryAcquire());
        now.addAndGet(4_999);
        assertFalse(cooldown.tryAcquire());
        now.addAndGet(1);
        assertTrue(cooldown.tryAcquire());
    }

    @Test
    void firstUseIsAllowedWhateverTheClock() {
        assertTrue(new Cooldown(10_000, () -> 0).tryAcquire());
    }
}
