package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDigitalBar;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.util.text.TextUtils;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOFrequencyDataScreen extends GuiInnerScreen {

    private final Supplier<QIOFrequency> frequencySupplier;

    public GuiQIOFrequencyDataScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<QIOFrequency> frequencySupplier) {
        super(gui, x, y, width, height);
        this.frequencySupplier = frequencySupplier;
        this.active = true;
        addChild(new GuiDigitalBar(gui, new IBarInfoHandler() {
            @Override
            public double getLevel() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? 0 : freq.getTotalItemCount() / (double) freq.getTotalItemCountCapacity();
            }

            @Override
            public ITextComponent getTooltip() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? null : MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity()));
            }
        }, relativeX + (width / 4) - (50 / 2), relativeY + 20, 50));
        addChild(new GuiDigitalBar(gui, new IBarInfoHandler() {
            @Override
            public double getLevel() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? 0 : freq.getTotalItemTypes(true) / (double) freq.getTotalItemTypeCapacity();
            }

            @Override
            public ITextComponent getTooltip() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? null : MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemTypes(true)), TextUtils.format(freq.getTotalItemTypeCapacity()));
            }
        }, relativeX + (3 * width / 4) - (50 / 2), relativeY + 20, 50));
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        QIOFrequency freq = frequencySupplier.get();
        if (freq != null) {
            drawScaledTextScaledBound(matrix, MekanismLang.FREQUENCY.translate(freq.getName()), relativeX + 5, relativeY + 5, screenTextColor(), width - 10, 0.8F);
        }
        drawScaledCenteredText(matrix, MekanismLang.QIO_ITEMS.translate(), relativeX + (width / 4), relativeY + 32, screenTextColor(), 0.8F);
        drawScaledCenteredText(matrix, MekanismLang.QIO_TYPES.translate(), relativeX + (3 * width / 4), relativeY + 32, screenTextColor(), 0.8F);
    }
}
