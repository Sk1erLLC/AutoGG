package club.sk1er.autogg.config;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.Property;
import club.sk1er.vigilance.data.PropertyType;
import club.sk1er.mods.core.util.MinecraftUtils;

import java.io.File;

public class AutoGGConfig extends Vigilant {

    @Property(
        type = PropertyType.SWITCH,
        name = "AutoGG",
        category = "General",
        subcategory = "General",
        description = "Toggle AutoGG entirely."
    )
    private boolean autoGGEnabled = true;

    @Property(
        type = PropertyType.SWITCH,
        name = "Casual AutoGG",
        category = "General",
        subcategory = "General",
        description = "Enable AutoGG for things that don't give Karma such as Skyblock Events."
    )
    private boolean casualAutoGGEnabled = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "AntiGG",
        category = "General",
        subcategory = "Miscellaneous",
        description = "Remove gg's from chat."
    )
    private boolean antiGGEnabled = false;

    @Property(
        type = PropertyType.SWITCH,
        name = "Anti Karma",
        category = "General",
        subcategory = "Miscellaneous",
        description = "Remove Karma messages from chat."
    )
    private boolean antiKarmaEnabled = false;

    @Property(
        type = PropertyType.SLIDER,
        name = "Delay",
        category = "General",
        subcategory = "General",
        description = "Delay after the game ends to say the message. (ms)",
        max = 5000
    )
    private int autoGGDelay = 1000;

    @Property(
        type = PropertyType.SELECTOR,
        name = "Phrase",
        category = "General",
        subcategory = "General",
        description = "Choose what message is said on game completion",
        options = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"}
    )
    private int autoGGPhrase = 1;

    @Property(
        type = PropertyType.SWITCH,
        name = "Second Message",
        category = "General",
        subcategory = "Secondary Message",
        description = "Enable a secondary message following your first GG"
    )
    private boolean secondaryEnabled = false;

    @Property(
        type = PropertyType.SELECTOR,
        name = "Phrase",
        category = "General",
        subcategory = "Secondary Message",
        description = "Choose a secondary message",
        options = {"Have a good day!", "<3", "AutoGG By Sk1er!"}
    )
    private int autoGGPhrase2 = 1;

    @Property(
        type = PropertyType.SLIDER,
        name = "Second Message Delay",
        category = "General",
        subcategory = "Secondary Message",
        description = "Delay between the first & second end of game messages. (ms)",
        max = 5000
    )
    private int secondaryDelay = 1000;

    public boolean isAutoGGEnabled() { return autoGGEnabled
//            && MinecraftUtils.isHypixel()
            ; }

    public boolean isCasualAutoGGEnabled() { return casualAutoGGEnabled
//            && MinecraftUtils.isHypixel()
            ; }

    public boolean isAntiGGEnabled() { return antiGGEnabled
//            && MinecraftUtils.isHypixel()
            ; }

    public boolean isAntiKarmaEnabled() { return antiKarmaEnabled
//            && MinecraftUtils.isHypixel()
            ; }

    public int getAutoGGDelay() {
        return autoGGDelay;
    }

    public int getAutoGGPhrase() {
        return autoGGPhrase;
    }

    public boolean isSecondaryEnabled() {
        return secondaryEnabled;
    }

    public int getAutoGGPhrase2() {
        return autoGGPhrase2;
    }

    public int getSecondaryDelay() {
        return secondaryDelay;
    }

    public AutoGGConfig() {
        super(new File("./config/autogg.toml"));
        initialize();
    }
}
