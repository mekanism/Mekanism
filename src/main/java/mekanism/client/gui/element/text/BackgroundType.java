package mekanism.client.gui.element.text;

import java.util.function.Consumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;

public enum BackgroundType {
    INNER_SCREEN(field -> GuiUtils.renderBackgroundTexture(GuiInnerScreen.SCREEN, 32, 32, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    ELEMENT_HOLDER(field -> GuiUtils.renderBackgroundTexture(GuiElementHolder.HOLDER, 2, 2, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    DEFAULT(field -> {
        GuiUtils.fill(field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.getWidth() + 2, field.getHeight() + 2, GuiTextField.DEFAULT_BORDER_COLOR);
        GuiUtils.fill(field.field_230690_l_, field.field_230691_m_, field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    DIGITAL(field -> {
        GuiUtils.fill(field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.getWidth() + 2, field.getHeight() + 2, field.isTextFieldFocused() ? GuiTextField.SCREEN_COLOR.getAsInt() : GuiTextField.DARK_SCREEN_COLOR.getAsInt());
        GuiUtils.fill(field.field_230690_l_, field.field_230691_m_, field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    NONE(field -> {
    });

    private final Consumer<GuiTextField> renderFunction;

    BackgroundType(Consumer<GuiTextField> renderFunction) {
        this.renderFunction = renderFunction;
    }

    public void render(GuiTextField field) {
        renderFunction.accept(field);
    }
}