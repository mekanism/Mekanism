package mekanism.client.recipe_viewer.emi.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(element.getGuiLeft(), element.getGuiTop(), 0);
        element.renderShifted(guiGraphics, mouseX, mouseY, 0);
        element.onDrawBackground(guiGraphics, mouseX, mouseY, 0);
        //Note: We don't care that onRenderForeground updates the maxZOffset in the mekanism gui as that is just used for rendering windows
        // and as our categories don't support windows we don't need to worry about that
        int zOffset = 200;
        pose.pushPose();
        element.onRenderForeground(guiGraphics, mouseX, mouseY, zOffset, zOffset);
        pose.popPose();
        pose.popPose();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        //TODO - 1.20.4: Figure out how to properly proxy the tooltips and then remove our manual cases where we use widgetHolder.addTooltip
        /*if (element.isMouseOver(mouseX, mouseY)) {
            element.renderToolTip(guiGraphics, mouseX, mouseY);
        }*/
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return forwardClicks && element.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return element.keyPressed(keyCode, scanCode, modifiers);
    }
}