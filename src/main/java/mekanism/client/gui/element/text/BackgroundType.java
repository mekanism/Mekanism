package mekanism.client.gui.element.text;

import java.util.function.BiConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphics;

public enum BackgroundType {
    INNER_SCREEN((field, guiGraphics) -> GuiUtils.renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    ELEMENT_HOLDER((field, guiGraphics) -> GuiUtils.renderBackgroundTexture(guiGraphics, GuiElementHolder.HOLDER, GuiElementHolder.HOLDER_SIZE, GuiElementHolder.HOLDER_SIZE, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2, 256, 256)),
    DEFAULT((field, guiGraphics) -> {
        GuiUtils.fill(guiGraphics, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2, GuiTextField.DEFAULT_BORDER_COLOR);
        GuiUtils.fill(guiGraphics, field.getRelativeX(), field.getRelativeY(), field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    DIGITAL((field, guiGraphics) -> {
        GuiUtils.fill(guiGraphics, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2, field.isTextFieldFocused() ? GuiTextField.SCREEN_COLOR.getAsInt() : GuiTextField.DARK_SCREEN_COLOR.getAsInt());
        GuiUtils.fill(guiGraphics, field.getRelativeX(), field.getRelativeY(), field.getWidth(), field.getHeight(), GuiTextField.DEFAULT_BACKGROUND_COLOR);
    }),
    NONE((field, guiGraphics) -> {
    });

    private final BiConsumer<GuiTextField, GuiGraphics> renderFunction;

    BackgroundType(BiConsumer<GuiTextField, GuiGraphics> renderFunction) {
        this.renderFunction = renderFunction;
    }

    public void render(GuiTextField field, GuiGraphics guiGraphics) {
        renderFunction.accept(field, guiGraphics);
    }
}