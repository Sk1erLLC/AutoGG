package club.sk1er.mods.autogg.tasks.data;

public enum TriggerType {
    NORMAL(0),
    CASUAL(1),
    ANTI_GG(2),
    ANTI_KARMA(3);

    private final int type;

    TriggerType(int type) {
        this.type = type;
    }

    public static TriggerType getByType(int t) {
        for (TriggerType type : values()) {
            if (type.type == t) return type;
        }
        return TriggerType.NORMAL; // Default
    }

    public int getType() {
        return type;
    }
}
