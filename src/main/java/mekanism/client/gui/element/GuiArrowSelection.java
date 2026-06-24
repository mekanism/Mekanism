package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiArrowSelection extends GuiTexturedElement {

    private static final Identifier ARROW = Mekanism.rl("arrow_selection");

    private final Supplier<@Nullable Component> targetText;

    public GuiArrowSelection(IGuiWrapper gui, int x, int y, Supplier<@Nullable Component> targetText) {
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
}