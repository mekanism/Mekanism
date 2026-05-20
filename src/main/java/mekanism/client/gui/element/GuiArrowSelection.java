package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GuiArrowSelection extends GuiTexturedElement {

    private static final Identifier ARROW = MekanismUtils.getResource(ResourceType.GUI, "arrow_selection.png");

    private final Supplier<Component> targetText;

    public GuiArrowSelection(IGuiWrapper gui, int x, int y, Supplier<Component> targetText) {
        super(ARROW, gui, x, y, 33, 19);
        this.targetText = targetText;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= getX() + 16 && xAxis < getRight() - 1 && yAxis >= getY() + 1 && yAxis < getBottom() - 1;
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        Component component = targetText.get();
        if (component != null) {
            drawScrollingString(guiGraphics, component, getWidth(), 6, TextAlignment.LEFT, screenTextColor(), 15, 1, false);
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }
}