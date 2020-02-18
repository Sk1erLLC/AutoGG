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
        type = PropertyType.SLIDER,
        name = "Delay",
        category = "General",
        subcategory = "General",
        description = "Delay after the game ends to say the message. (ms)",
        max = 5000
    )
    public static int autoGGDelay = 1000;


    @Property(
        type = PropertyType.SELECTOR,
        name = "Phrase",
        category = "General",
        subcategory = "General",
        description = "Choose what message is said on game completion",
        options = {"gg", "GG", "gf", "Good Game", "Good Fight"}
    )
    public static int autoGGPhrase = 1;


    @Property(
        type = PropertyType.SWITCH,
        name = "Second Message",
        category = "General",
        subcategory = "Secondary Message",
        description = "Enable a secondary message following your first GG"
    )
    public static boolean secondaryEnabled = false;

    @Property(
        type = PropertyType.SELECTOR,
        name = "Phrase",
        category = "General",
        subcategory = "Secondary Message",
        description = "Choose a secondary message",
        options = {"Have a good day!", "<3", "AutoGG By Sk1er!"}
    )
    public static int autoGGPhrase2 = 1;

    @Property(
        type = PropertyType.SLIDER,
        name = "Second Message Delay",
        category = "General",
        subcategory = "Secondary Message",
        description = "Delay between the first and second end of game messages. (ms)",
        max = 5000
    )
    public static int secondaryDelay = 1000;


    public AutoGGConfig() {
        super(new File("./config/autogg.toml"));
        initialize();
    }
}
