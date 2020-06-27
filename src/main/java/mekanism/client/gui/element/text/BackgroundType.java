package mekanism.client.gui.element.text;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BiConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;

public enum BackgroundType {
    INNER_SCREEN((field, matrix) -> GuiUtils.renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, 32, 32, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.func_230998_h_() + 2, field.getHeight() + 2, 256, 256)),
    ELEMENT_HOLDER((field, matrix) -> GuiUtils.renderBackgroundTexture(matrix, GuiElementHolder.HOLDER, 2, 2, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.func_230998_h_() + 2, field.getHeight() + 2, 256, 256)),
    DEFAULT((field, matrix) -> {
        GuiUtils.fill(matrix, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.func_230998_h_() + 2, field.getHeight() + 2, GuiTextField.DEFAULT_BORDER_COLOR);
        GuiUtils.fill(matrix, field.field_230690_l_, field.field_230691_m_, field.func_230998_h_(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    DIGITAL((field, matrix) -> {
        GuiUtils.fill(matrix, field.field_230690_l_ - 1, field.field_230691_m_ - 1, field.func_230998_h_() + 2, field.getHeight() + 2, field.isTextFieldFocused() ? GuiTextField.SCREEN_COLOR.getAsInt() : GuiTextField.DARK_SCREEN_COLOR.getAsInt());
        GuiUtils.fill(matrix, field.field_230690_l_, field.field_230691_m_, field.func_230998_h_(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    NONE((field, matrix) -> {
    });

    private final BiConsumer<GuiTextField, MatrixStack> renderFunction;

    BackgroundType(BiConsumer<GuiTextField, MatrixStack> renderFunction) {
        this.renderFunction = renderFunction;
    }

    public void render(GuiTextField field, MatrixStack matrix) {
        renderFunction.accept(field, matrix);
    }
}