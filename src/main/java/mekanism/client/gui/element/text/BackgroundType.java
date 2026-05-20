package mekanism.client.gui.element.text;

import java.util.function.BiConsumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public enum BackgroundType {
    INNER_SCREEN((field, guiGraphics) -> guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiInnerScreen.SCREEN, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2)),
    ELEMENT_HOLDER((field, guiGraphics) -> guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiElementHolder.HOLDER, field.getRelativeX() - 1, field.getRelativeY() - 1, field.getWidth() + 2, field.getHeight() + 2)),
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

    private final BiConsumer<GuiTextField, GuiGraphicsExtractor> renderFunction;

    BackgroundType(BiConsumer<GuiTextField, GuiGraphicsExtractor> renderFunction) {
        this.renderFunction = renderFunction;
    }

    public void render(GuiTextField field, GuiGraphicsExtractor guiGraphics) {
        renderFunction.accept(field, guiGraphics);
    }
}