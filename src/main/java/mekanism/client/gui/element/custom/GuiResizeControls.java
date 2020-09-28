package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
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
import net.minecraft.util.ResourceLocation;

public class GuiResizeControls extends GuiSideHolder {

    private final MekanismImageButton expandButton, shrinkButton;
    private final Consumer<ResizeType> resizeHandler;

    private int tooltipTicks;

    private static final ResourceLocation MINUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "minus.png");
    private static final ResourceLocation PLUS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "plus.png");

    public GuiResizeControls(IGuiWrapper gui, int y, Consumer<ResizeType> resizeHandler) {
        super(gui, -26, y, 39, true);
        this.resizeHandler = resizeHandler;
        addChild(expandButton = new MekanismImageButton(gui, this.x + 4, this.y + 5, 19, 9, 19, 9, PLUS, () -> handleResize(ResizeType.EXPAND_Y)));
        addChild(shrinkButton = new MekanismImageButton(gui, this.x + 4, this.y + 25, 19, 9, 19, 9, MINUS, () -> handleResize(ResizeType.SHRINK_Y)));
        updateButtonState();
        active = true;
    }

    @Override
    public void tick() {
        super.tick();
        tooltipTicks = Math.max(0, tooltipTicks - 1);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        if (tooltipTicks > 0 && !expandButton.active) {
            displayTooltip(matrix, MekanismLang.QIO_COMPENSATE_TOOLTIP.translate(), mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawScaledCenteredText(matrix, MekanismLang.HEIGHT.translate(), relativeX + 13.5F, relativeY + 15.5F, titleTextColor(), 0.7F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (!expandButton.active && mouseX >= expandButton.x && mouseX < expandButton.x + expandButton.getWidth() &&
            mouseY >= expandButton.y && mouseY < expandButton.y + expandButton.getHeightRealms()) {
            tooltipTicks = 100;
        }
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_RESIZE_CONTROLS);
    }

    private void handleResize(ResizeType type) {
        resizeHandler.accept(type);
        updateButtonState();
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
}
