package club.sk1er.mods.autogg.tasks.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TriggerTypeTest {
    @Test
    void mapsTheTriggersFileTypes() {
        assertEquals(TriggerType.NORMAL, TriggerType.getByType(0));
        assertEquals(TriggerType.CASUAL, TriggerType.getByType(1));
        assertEquals(TriggerType.ANTI_GG, TriggerType.getByType(2));
        assertEquals(TriggerType.ANTI_KARMA, TriggerType.getByType(3));
    }

    @Test
    void unknownTypesAreNormal() {
        assertEquals(TriggerType.NORMAL, TriggerType.getByType(42));
    }
}
