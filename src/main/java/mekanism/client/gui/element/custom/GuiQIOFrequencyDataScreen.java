package mekanism.client.gui.element.custom;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiDigitalBar;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

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
            public Component getTooltip() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? null : MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity()));
            }
        }, relativeX + 11, relativeY + 20, 50));
        addChild(new GuiDigitalBar(gui, new IBarInfoHandler() {
            @Override
            public double getLevel() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? 0 : freq.getTotalItemTypes(true) / (double) freq.getTotalItemTypeCapacity();
            }

            @Override
            public Component getTooltip() {
                QIOFrequency freq = frequencySupplier.get();
                return freq == null ? null : MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemTypes(true)), TextUtils.format(freq.getTotalItemTypeCapacity()));
            }
        }, relativeX + 83, relativeY + 20, 50));
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        QIOFrequency freq = frequencySupplier.get();
        if (freq != null) {
            drawScaledScrollingString(guiGraphics, MekanismLang.FREQUENCY.translate(freq.getName()), 0, 5, TextAlignment.LEFT, screenTextColor(), 5, false, 0.8F);
        }
        drawScaledScrollingString(guiGraphics, MekanismLang.QIO_ITEMS.translate(), 11, 32, TextAlignment.CENTER, screenTextColor(), 50, 0, false, 0.8F);
        drawScaledScrollingString(guiGraphics, MekanismLang.QIO_TYPES.translate(), 83, 32, TextAlignment.CENTER, screenTextColor(), 50, 0, false, 0.8F);
    }
}
