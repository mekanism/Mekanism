package mekanism.client.gui.element.custom;

import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class GuiResizeControls extends GuiSideHolder {

    private static final ResourceLocation MINUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "minus.png");
    private static final ResourceLocation PLUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "plus.png");

    public GuiResizeControls(IGuiWrapper gui, int y, Consumer<ResizeType> resizeHandler) {
        super(gui, -26, y, 48);
        addChild(new MekanismImageButton(gui, gui.getLeft() + -22, gui.getTop() + y + 14, 9, MINUS, () -> resizeHandler.accept(ResizeType.SHRINK_Y)));
        addChild(new MekanismImageButton(gui, gui.getLeft() + -12, gui.getTop() + y + 14, 9, PLUS, () -> resizeHandler.accept(ResizeType.EXPAND_Y)));
        addChild(new MekanismImageButton(gui, gui.getLeft() + -22, gui.getTop() + y + 32, 9, MINUS, () -> resizeHandler.accept(ResizeType.SHRINK_X)));
        addChild(new MekanismImageButton(gui, gui.getLeft() + -12, gui.getTop() + y + 32, 9, PLUS, () -> resizeHandler.accept(ResizeType.EXPAND_X)));
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawScaledCenteredText(new StringTextComponent("Height"), relativeX + 13.5F, relativeY + 5, titleTextColor(), 0.7F);
        drawScaledCenteredText(new StringTextComponent("Width"), relativeX + 13.5F, relativeY + 24, titleTextColor(), 0.7F);
    }

    public enum ResizeType {
        EXPAND_X,
        EXPAND_Y,
        SHRINK_X,
        SHRINK_Y;
    }
}
