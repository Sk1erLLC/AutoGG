/*
 * AutoGG - Automatically say a selectable phrase at the end of a game on supported servers.
 * Copyright (C) 2020  Sk1er LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.sk1er.mods.autogg.config;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.Property;
import club.sk1er.vigilance.data.PropertyType;

import java.io.File;

@SuppressWarnings("FieldMayBeFinal")
public class AutoGGConfig extends Vigilant {

    @Property(
        type = PropertyType.SWITCH, name = "AutoGG",
        description = "Toggle AutoGG entirely.",
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
    private int autoGGPhrase = 1;

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
    private int autoGGPhrase2 = 1;

    @Property(
        type = PropertyType.SLIDER, name = "Second Message Delay",
        description = "Delay between the first & second end of game messages.\n§eMeasured in milliseconds.",
        category = "General", subcategory = "Secondary Message",
        max = 5000
    )
    private int secondaryDelay = 1000;

    public boolean toggle() {
        autoGGEnabled = !autoGGEnabled;
        markDirty(); // required since directly writing to vars
        writeData();
        return autoGGEnabled;
    }

    public boolean isAutoGGEnabled() {
        return autoGGEnabled && AutoGG.instance.works();
    }

    public boolean isCasualAutoGGEnabled() {
        return casualAutoGGEnabled && AutoGG.instance.works();
    }

    public boolean isAntiGGEnabled() {
        return antiGGEnabled && AutoGG.instance.works();
    }

    public boolean isAntiKarmaEnabled() {
        return antiKarmaEnabled && AutoGG.instance.works();
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

    public AutoGGConfig() {
        super(new File("./config/autogg.toml"));
        initialize();
    }
}
