package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSideHolder;
import mekanism.client.gui.element.button.MekanismImageButton;
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
        addChild(expandButton = new MekanismImageButton(gui, this.field_230690_l_ + 4, this.field_230691_m_ + 5, 19, 9, 19, 9, PLUS, () -> handleResize(ResizeType.EXPAND_Y)));
        addChild(shrinkButton = new MekanismImageButton(gui, this.field_230690_l_ + 4, this.field_230691_m_ + 25, 19, 9, 19, 9, MINUS, () -> handleResize(ResizeType.SHRINK_Y)));
        updateButtonState();
        field_230693_o_ = true;
    }

    @Override
    public void tick() {
        super.tick();
        tooltipTicks = Math.max(0, tooltipTicks - 1);
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.func_230443_a_(matrix, mouseX, mouseY);
        if (tooltipTicks > 0 && !expandButton.field_230693_o_) {
            displayTooltip(matrix, MekanismLang.QIO_COMPENSATE_TOOLTIP.translate(), mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawScaledCenteredText(matrix, MekanismLang.HEIGHT.translate(), relativeX + 13.5F, relativeY + 15.5F, titleTextColor(), 0.7F);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        super.func_230982_a_(mouseX, mouseY);
        if (!expandButton.field_230693_o_ && mouseX >= expandButton.field_230690_l_ && mouseX < expandButton.field_230690_l_ + expandButton.func_230998_h_() &&
            mouseY >= expandButton.field_230691_m_ && mouseY < expandButton.field_230691_m_ + expandButton.getHeight()) {
            tooltipTicks = 100;
        }
    }

    private void handleResize(ResizeType type) {
        resizeHandler.accept(type);
        updateButtonState();
    }

    private void updateButtonState() {
        int index = getIndex();
        expandButton.field_230693_o_ = index < QIOItemViewerContainer.getSlotsYMax();
        shrinkButton.field_230693_o_ = index > QIOItemViewerContainer.SLOTS_Y_MIN;
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
