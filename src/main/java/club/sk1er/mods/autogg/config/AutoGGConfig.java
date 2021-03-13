package club.sk1er.mods.autogg.config;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.Property;
import club.sk1er.vigilance.data.PropertyType;

import java.io.File;

@SuppressWarnings("FieldMayBeFinal")
public class AutoGGConfig extends Vigilant {
    @Property(
        type = PropertyType.SWITCH, name = "AutoGG",
        description = "Entirely toggles AutoGG",
        category = "General", subcategory = "General"
    )
    private boolean autoGGEnabled = true;

    @Property(
        type = PropertyType.SWITCH, name = "Casual AutoGG",
        description = "Enable AutoGG for things that don't give Karma such as Skyblock Events.",
        category = "General", subcategory = "General"
    )
    private boolean casualAutoGGEnabled;

    @Property(
        type = PropertyType.SWITCH, name = "Anti GG",
        description = "Remove GG messages from chat.",
        category = "General", subcategory = "Miscellaneous"
    )
    private boolean antiGGEnabled;

    @Property(
        type = PropertyType.SWITCH, name = "Anti Karma",
        description = "Remove Karma messages from chat.",
        category = "General", subcategory = "Miscellaneous"
    )
    private boolean antiKarmaEnabled;

    @Property(
        type = PropertyType.SLIDER, name = "Delay",
        description = "Delay after the game ends to say the message.\n§eMeasured in milliseconds.",
        category = "General", subcategory = "General",
        max = 5000
    )
    private int autoGGDelay = 1000;

    @Property(
        type = PropertyType.SELECTOR, name = "Phrase",
        description = "Choose what message is said on game completion.",
        category = "General", subcategory = "General",
        options = {"gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D"}
    )
    private int autoGGPhrase = 0;

    @Property(
        type = PropertyType.SWITCH, name = "Second Message",
        description = "Enable a secondary message to send after your first GG.",
        category = "General", subcategory = "Secondary Message"
    )
    private boolean secondaryEnabled;

    @Property(
        type = PropertyType.SELECTOR, name = "Phrase",
        description = "Send a secondary message sent after the first GG message.",
        category = "General", subcategory = "Secondary Message",
        options = {"Have a good day!", "<3", "AutoGG By Sk1er!"}
    )
    private int autoGGPhrase2 = 0;

    @Property(
        type = PropertyType.SLIDER, name = "Second Message Delay",
        description = "Delay between the first & second end of game messages.\n§eMeasured in milliseconds.",
        category = "General", subcategory = "Secondary Message",
        max = 5000
    )
    private int secondaryDelay = 1000;

    public AutoGGConfig() {
        super(new File(".config/autogg.toml"));
        initialize();
    }

    public boolean isModEnabled() {
        return autoGGEnabled;
    }

    public boolean isCasualAutoGGEnabled() {
        return casualAutoGGEnabled;
    }

    public boolean isAntiGGEnabled() {
        return antiGGEnabled;
    }

    public boolean isAntiKarmaEnabled() {
        return antiKarmaEnabled;
    }

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
}
