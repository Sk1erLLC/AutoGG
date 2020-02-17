package club.sk1er.autogg.config;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.Property;
import club.sk1er.vigilance.data.PropertyType;

import java.io.File;

public class AutoGGConfig extends Vigilant {

    @Property(
            type = PropertyType.SWITCH,
            name = "AutoGG",
            category = "General",
            subcategory = "General",
            description = "Toggle AutoGG entirely."
    )
    public static boolean autoGGEnabled = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "AntiGG",
            category = "General",
            subcategory = "Miscellaneous",
            description = "Remove gg's from chat."
    )
    public static boolean antiGGEnabled = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Good Game",
            category = "General",
            subcategory = "Miscellaneous",
            description = "Say Good Game instead of gg."
    )
    public static boolean goodGameEnabled = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Lowercase",
            category = "General",
            subcategory = "Miscellaneous",
            description = "Use gg/good game instead of GG/Good Game."
    )
    public static boolean lowercaseEnabled = true;

    @Property(
            type = PropertyType.SLIDER,
            name = "Delay",
            category = "General",
            subcategory = "General",
            description = "Delay after the game ends to say the message.",
            max = 5
    )
    public static int autoGGDelay = 1;

    public AutoGGConfig() {
        super(new File("./config/autogg.toml"));
        initialize();
    }
}
