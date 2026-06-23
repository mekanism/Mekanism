package mekanism.client.gui.element.custom;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiResizeControls extends GuiSideHolder {

    @Nullable
    private static final Tooltip COMPENSATE = TooltipUtils.create(MekanismLang.QIO_COMPENSATE_TOOLTIP);

    private final MekanismImageButton expandButton, shrinkButton;

    private int tooltipTicks;

    private static final Identifier MINUS = Mekanism.rl("button/minus");
    private static final Identifier PLUS = Mekanism.rl("button/plus");

    public <GUI extends IGuiWrapper & ResizeController> GuiResizeControls(GUI gui, int y) {
        super(gui, -26, y, 40, true, false);
        expandButton = addChild(new MekanismImageButton(gui, relativeX + 4, relativeY + 5, 19, 9, PLUS,
              (_, event, _) -> handleResize(ResizeType.EXPAND_Y, event.hasShiftDown())));
        shrinkButton = addChild(new MekanismImageButton(gui, relativeX + 4, relativeY + 26, 19, 9, MINUS,
              (_, event, _) -> handleResize(ResizeType.SHRINK_Y, event.hasShiftDown())));
        updateButtonState();
        active = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (tooltipTicks > 0) {
            tooltipTicks--;
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (tooltipTicks > 0 && !expandButton.active) {
            setTooltip(COMPENSATE);
        } else {
            clearTooltip();
        }
    }

    @Override
    public void renderForeground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawScaledScrollingString(guiGraphics, MekanismLang.HEIGHT.translate(), 0, 16, TextAlignment.CENTER, titleTextColor(), 4, false, 0.6F);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        if (!expandButton.active && event.x() >= expandButton.getX() && event.x() < expandButton.getRight() && event.y() >= expandButton.getY() && event.y() < expandButton.getBottom()) {
            tooltipTicks = 5 * SharedConstants.TICKS_PER_SECOND;
        }
    }

    @Override
    protected int getTabColor(GuiGraphicsExtractor guiGraphics) {
        return MekanismRenderer.color(SpecialColors.TAB_RESIZE_CONTROLS);
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
