package club.sk1er.mods.autogg.tasks.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Server {
    private final String name;

    private final String handler;

    private final String detector;

    private final String messagePrefix;

    private final String[] triggers;

    private final String[] casualTriggers;

    private final String antiGGTrigger;

    private final String antiKarmaTrigger;

    private DetectorHandler detectorHandler;

    public Server(@NotNull String name, @NotNull String detectorHandler, @NotNull String detector, @NotNull String messagePrefix, @NotNull String[] triggers, @NotNull String[] casualTriggers, @Nullable String antiGGTrigger, @Nullable String antiKarmaTrigger) {
        this.name = name;
        this.handler = detectorHandler;
        this.detector = detector;
        this.messagePrefix = messagePrefix;
        this.triggers = triggers;
        this.casualTriggers = casualTriggers;
        this.antiGGTrigger = antiGGTrigger;
        this.antiKarmaTrigger = antiKarmaTrigger;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public DetectorHandler getHandler() {
        if (detectorHandler == null) detectorHandler = DetectorHandler.valueOf(handler);
        return detectorHandler;
    }

    @NotNull
    public String getDetector() {
        return detector;
    }

    @NotNull
    public String[] getTriggers() {
        return triggers;
    }

    @NotNull
    public String[] getCasualTriggers() {
        return casualTriggers;
    }

    @NotNull
    public String getMessagePrefix() {
        return messagePrefix;
    }

    @Nullable
    public String getAntiGGTrigger() {
        return antiGGTrigger;
    }

    @Nullable
    public String getAntiKarmaTrigger() {
        return antiKarmaTrigger;
    }
}
