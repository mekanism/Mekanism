package mekanism.client.gui.element.text;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BiConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;

public enum BackgroundType {
    INNER_SCREEN((field, matrix) -> GuiUtils.renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE, field.x - 1, field.y - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    ELEMENT_HOLDER((field, matrix) -> GuiUtils.renderBackgroundTexture(matrix, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, GuiElementHolder.HOLDER_SIZE, field.x - 1, field.y - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    DEFAULT((field, matrix) -> {
        GuiUtils.fill(matrix, field.x - 1, field.y - 1, field.getWidth() + 2, field.getHeight() + 2, GuiTextField.DEFAULT_BORDER_COLOR);
        GuiUtils.fill(matrix, field.x, field.y, field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    DIGITAL((field, matrix) -> {
        GuiUtils.fill(matrix, field.x - 1, field.y - 1, field.getWidth() + 2, field.getHeight() + 2, field.isTextFieldFocused() ? GuiTextField.SCREEN_COLOR.getAsInt() : GuiTextField.DARK_SCREEN_COLOR.getAsInt());
        GuiUtils.fill(matrix, field.x, field.y, field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    NONE((field, matrix) -> {
    });

    private final BiConsumer<GuiTextField, PoseStack> renderFunction;

    BackgroundType(BiConsumer<GuiTextField, PoseStack> renderFunction) {
        this.renderFunction = renderFunction;
    }

    public void render(GuiTextField field, PoseStack matrix) {
        renderFunction.accept(field, matrix);
    }
}