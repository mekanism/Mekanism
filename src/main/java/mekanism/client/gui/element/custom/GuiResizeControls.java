package mekanism.client.gui.element.custom;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiResizeControls extends GuiSideHolder {

    private final MekanismImageButton expandButton, shrinkButton;

    private int tooltipTicks;

    private static final ResourceLocation MINUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "minus.png");
    private static final ResourceLocation PLUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "plus.png");

    public <GUI extends IGuiWrapper & ResizeController> GuiResizeControls(GUI gui, int y) {
        super(gui, -26, y, 39, true, false);
        expandButton = addChild(new MekanismImageButton(gui, relativeX + 4, relativeY + 5, 19, 9, 19, 9, PLUS,
              (element, mouseX, mouseY) -> ((GuiResizeControls) element).handleResize(ResizeType.EXPAND_Y, Screen.hasShiftDown())));
        shrinkButton = addChild(new MekanismImageButton(gui, relativeX + 4, relativeY + 25, 19, 9, 19, 9, MINUS,
              (element, mouseX, mouseY) -> ((GuiResizeControls) element).handleResize(ResizeType.SHRINK_Y, Screen.hasShiftDown())));
        updateButtonState();
        active = true;
    }

    @Override
    public void tick() {
        super.tick();
        tooltipTicks = Math.max(0, tooltipTicks - 1);
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        if (tooltipTicks > 0 && !expandButton.active) {
            displayTooltips(guiGraphics, mouseX, mouseY, MekanismLang.QIO_COMPENSATE_TOOLTIP.translate());
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawScaledCenteredTextScaledBound(guiGraphics, MekanismLang.HEIGHT.translate(), relativeX + 13.5F, relativeY + 15.5F, titleTextColor(), width - 4, 0.7F);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        if (!expandButton.active && mouseX >= expandButton.getX() && mouseX < expandButton.getRight() && mouseY >= expandButton.getY() && mouseY < expandButton.getBottom()) {
            tooltipTicks = 5 * SharedConstants.TICKS_PER_SECOND;
        }
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_RESIZE_CONTROLS);
    }

    private boolean handleResize(ResizeType type, boolean adjustMax) {
        //Validate something didn't change and it still is actually a controller
        if (gui() instanceof ResizeController resizeHandler) {
            resizeHandler.resize(type, adjustMax);
            updateButtonState();
            return true;
        }
        return false;
    }

    private void updateButtonState() {
        int index = getIndex();
        expandButton.active = index < QIOItemViewerContainer.getSlotsYMax();
        shrinkButton.active = index > QIOItemViewerContainer.SLOTS_Y_MIN;
    }

    private int getIndex() {
        return MekanismConfig.client.qioItemViewerSlotsY.get();
    }

    public enum ResizeType {
        EXPAND_X,
        EXPAND_Y,
        SHRINK_X,
        SHRINK_Y;
    }

    @FunctionalInterface
    public interface ResizeController {

        void resize(ResizeType type, boolean adjustMax);
    }
}
