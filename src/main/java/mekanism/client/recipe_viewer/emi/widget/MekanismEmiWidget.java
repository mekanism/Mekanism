package mekanism.client.recipe_viewer.emi.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.joml.Matrix3x2fStack;

@NothingNullByDefault
public class MekanismEmiWidget extends Widget {

    private final boolean forwardClicks;
    private final GuiElement element;
    private final Bounds bounds;

    public MekanismEmiWidget(GuiElement element, boolean forwardClicks) {
        this.element = element;
        this.forwardClicks = forwardClicks;
        this.bounds = new Bounds(element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(element.getGuiLeft(), element.getGuiTop(), 0);
        element.renderShifted(guiGraphics, mouseX, mouseY, 0);
        element.onDrawBackground(guiGraphics, mouseX, mouseY, 0);
        //Note: We don't care that onRenderForeground updates the maxZOffset in the mekanism gui as that is just used for rendering windows
        // and as our categories don't support windows we don't need to worry about that
        int zOffset = 200;
        pose.popMatrix();
        element.onRenderForeground(guiGraphics, mouseX, mouseY);
        pose.popMatrix();
        pose.popMatrix();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        //Note: EMI only calls this method if we are over it
        //Start by updating the tooltip for the element in case it is conditionally dependent on the mouse position
        element.updateTooltip(mouseX, mouseY);
        Tooltip tooltip = element.tooltip.get();
        if (tooltip != null) {
            return tooltip.toCharSequence(Minecraft.getInstance()).stream()
                  .map(ClientTooltipComponent::create)
                  .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return forwardClicks && element.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return element.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }
}