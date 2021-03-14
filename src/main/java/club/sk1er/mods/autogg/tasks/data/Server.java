package club.sk1er.mods.autogg.tasks.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Server {
    private final String name;

    private final String kind;

    private final String data;

    private final String messagePrefix;

    private final Trigger[] triggers;

    private DetectorHandler detectorHandler;

    public Server(@NotNull String name, @NotNull String kind, @NotNull String data, @NotNull String messagePrefix, @NotNull Trigger[] triggers, @NotNull String[] casualTriggers, @Nullable String antiGGTrigger, @Nullable String antiKarmaTrigger) {
        this.name = name;
        this.kind = kind;
        this.data = data;
        this.messagePrefix = messagePrefix;
        this.triggers = triggers;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public DetectorHandler getDetectionHandler() {
        if (detectorHandler == null) detectorHandler = DetectorHandler.valueOf(kind);
        return detectorHandler;
    }

    @NotNull
    public String getData() {
        return data;
    }

    @NotNull
    public Trigger[] getTriggers() {
        return triggers;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }
}
