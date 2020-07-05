package mekanism.client.gui.element.custom;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiColorPickerSlot;
import mekanism.client.gui.element.GuiSlider;
import mekanism.client.gui.element.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Color;

public class GuiMekaSuitHelmetOptions extends GuiWindow {

    public GuiMekaSuitHelmetOptions(IGuiWrapper gui, int x, int y) {
        super(gui, x, y, 140, 65);
        interactionStrategy = InteractionStrategy.NONE;
        addChild(new GuiColorPickerSlot(gui, x + 7, y + 32, () -> Color.argb(MekanismConfig.client.hudColor.get()), color -> {
            MekanismConfig.client.hudColor.set(color.argb());
            // save the updated config info
            MekanismConfig.client.getConfigSpec().save();
        }));

        GuiSlider slider;
        addChild(slider = new GuiSlider(gui, x + 30, y + 35, 100, value -> {
            MekanismConfig.client.hudColor.set(Color.argb(MekanismConfig.client.hudColor.get()).alpha(value).argb());
        }));
        slider.setValue(Color.argb(MekanismConfig.client.hudColor.get()).ad());
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        drawCenteredTextScaledBound(matrix, MekanismLang.HELMET_OPTIONS.translate(), 120, 6, titleTextColor());
        drawTextExact(matrix, MekanismLang.HUD_OVERLAY.translate(), relativeX + 7, relativeY + 20, headingTextColor());

        drawCenteredText(matrix, MekanismLang.OPACITY.translate(), relativeX + 30, 100, relativeY + 48, subheadingTextColor());
    }
}
