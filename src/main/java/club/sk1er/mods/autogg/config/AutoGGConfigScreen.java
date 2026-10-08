package club.sk1er.mods.autogg.config;

import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.handlers.patterns.GGPhrases;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * The settings screen opened by /autogg, with the settings and descriptions of the Vigilance GUI it replaces.
 */
public class AutoGGConfigScreen extends Screen {
    private static final int COLUMN_WIDTH = 155;

    private final Screen parent;
    private final AutoGGConfig config = AutoGG.INSTANCE.getAutoGGConfig();
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public AutoGGConfigScreen(@Nullable Screen parent) {
        super(Component.literal("AutoGG"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(title, font);

        GridLayout grid = new GridLayout().columnSpacing(10).rowSpacing(4);
        GridLayout.RowHelper rows = grid.createRowHelper(2);

        section(rows, "General");
        rows.addChild(toggle("AutoGG", "Entirely toggles AutoGG", config.isModEnabled(), config::setModEnabled));
        rows.addChild(toggle("Casual AutoGG", "Enable AutoGG for things that don't give Karma such as Skyblock Events.", config.isCasualAutoGGEnabled(), config::setCasualAutoGGEnabled));
        rows.addChild(new DelaySlider("Delay", "Delay after the game ends to say the message.\nMeasured in seconds.", config.getClampedAutoGGDelay(), config::setAutoGGDelay));
        rows.addChild(phrase("Phrase", "Choose what message is said on game completion.", GGPhrases.PRIMARY, config.getAutoGGPhrase(), config::setAutoGGPhrase, COLUMN_WIDTH));

        section(rows, "Miscellaneous");
        rows.addChild(toggle("Anti GG", "Remove GG messages from chat.", config.isAntiGGEnabled(), config::setAntiGGEnabled));
        rows.addChild(toggle("Anti Karma", "Remove Karma messages from chat.", config.isAntiKarmaEnabled(), config::setAntiKarmaEnabled));

        section(rows, "Secondary Message");
        rows.addChild(toggle("Second Message", "Enable a secondary message to send after your first GG.", config.isSecondaryEnabled(), config::setSecondaryEnabled));
        rows.addChild(new DelaySlider("Second Message Delay", "Delay between the first & second end of game messages.\nMeasured in seconds.", config.getClampedSecondaryDelay(), config::setSecondaryDelay));
        rows.addChild(phrase("Second Message Phrase", "Send a secondary message sent after the first GG message.", GGPhrases.SECONDARY, config.getAutoGGPhrase2(), config::setAutoGGPhrase2, COLUMN_WIDTH * 2 + 10), 2);

        layout.addToContents(grid);
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(200).build());
        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        config.save();
        //? if >= 26.2 {
        minecraft.gui.setScreen(parent);
        //?} else {
        // minecraft.setScreen(parent);
        //?}
    }

    private void section(GridLayout.RowHelper rows, String name) {
        rows.addChild(new StringWidget(Component.literal(name).withStyle(ChatFormatting.YELLOW), font), 2, rows.newCellSettings().alignHorizontallyCenter().paddingTop(4));
    }

    private static CycleButton<Boolean> toggle(String name, String description, boolean value, BooleanSetter setter) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(value).create(Component.literal(name), (cycleButton, newValue) -> setter.set(newValue));
        button.setTooltip(Tooltip.create(Component.literal(description)));
        button.setWidth(COLUMN_WIDTH);
        return button;
    }

    private static CycleButton<Integer> phrase(String name, String description, String[] phrases, int value, IntConsumer setter, int width) {
        CycleButton<Integer> button = CycleButton.builder((Integer index) -> Component.literal(phrases[index]), AutoGGConfig.clampPhrase(value, phrases.length))
                .withValues(IntStream.range(0, phrases.length).boxed().toList())
                .create(Component.literal(name), (cycleButton, newValue) -> setter.accept(newValue));
        button.setTooltip(Tooltip.create(Component.literal(description)));
        button.setWidth(width);
        return button;
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }

    /**
     * A whole-second slider from 0 to {@link AutoGGConfig#MAX_DELAY}, like the Vigilance one.
     */
    private static class DelaySlider extends AbstractSliderButton {
        private final String name;
        private final IntConsumer setter;

        DelaySlider(String name, String description, int seconds, IntConsumer setter) {
            super(0, 0, COLUMN_WIDTH, 20, Component.empty(), AutoGGConfig.secondsToSlider(seconds));
            this.name = name;
            this.setter = setter;
            setTooltip(Tooltip.create(Component.literal(description)));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(name + ": " + AutoGGConfig.sliderToSeconds(value) + "s"));
        }

        @Override
        protected void applyValue() {
            setter.accept(AutoGGConfig.sliderToSeconds(value));
        }
    }
}
