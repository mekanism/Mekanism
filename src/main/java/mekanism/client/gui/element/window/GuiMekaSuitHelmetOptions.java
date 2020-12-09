package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiColorPickerSlot;
import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.client.gui.element.GuiSlider;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.HUDElement.HUDColor;
import mekanism.common.lib.Color;

public class GuiMekaSuitHelmetOptions extends GuiWindow {

    public GuiMekaSuitHelmetOptions(IGuiWrapper gui, int x, int y) {
        super(gui, x, y, 140, 115);
        interactionStrategy = InteractionStrategy.NONE;
        addChild(new GuiColorPickerSlot(gui, x + 12, y + 32, () -> Color.argb(HUDColor.REGULAR.getColor()), color -> {
            MekanismConfig.client.hudColor.set(color.argb());
            // save the updated config info
            MekanismConfig.client.getConfigSpec().save();
        }));
        addChild(new GuiColorPickerSlot(gui, x + 61, y + 32, () -> Color.argb(HUDColor.WARNING.getColor()), color -> {
            MekanismConfig.client.hudWarningColor.set(color.argb());
            // save the updated config info
            MekanismConfig.client.getConfigSpec().save();
        }));
        addChild(new GuiColorPickerSlot(gui, x + 110, y + 32, () -> Color.argb(HUDColor.DANGER.getColor()), color -> {
            MekanismConfig.client.hudDangerColor.set(color.argb());
            // save the updated config info
            MekanismConfig.client.getConfigSpec().save();
        }));

        GuiSlider slider;
        addChild(slider = new GuiSlider(gui, x + 10, y + 62, 120, value -> MekanismConfig.client.hudOpacity.set((float) value)));
        slider.setValue(MekanismConfig.client.hudOpacity.get());

        addChild(new GuiScreenSwitch(gui, x + 7, y + 87, 126, MekanismLang.COMPASS.translate(), MekanismConfig.client.hudCompassEnabled, () -> {
            MekanismConfig.client.hudCompassEnabled.set(!MekanismConfig.client.hudCompassEnabled.get());
            // save the updated config info
            MekanismConfig.client.getConfigSpec().save();
        }));
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);

        drawCenteredTextScaledBound(matrix, MekanismLang.HELMET_OPTIONS.translate(), 120, 6, titleTextColor());
        drawTextExact(matrix, MekanismLang.HUD_OVERLAY.translate(), relativeX + 7, relativeY + 20, headingTextColor());

        drawScaledCenteredText(matrix, MekanismLang.DEFAULT.translate(), relativeX + 21, relativeY + 52, subheadingTextColor(), 0.8F);
        drawScaledCenteredText(matrix, MekanismLang.WARNING.translate(), relativeX + 70, relativeY + 52, subheadingTextColor(), 0.8F);
        drawScaledCenteredText(matrix, MekanismLang.DANGER.translate(), relativeX + 119, relativeY + 52, subheadingTextColor(), 0.8F);

        drawScaledCenteredText(matrix, MekanismLang.OPACITY.translate(), relativeX + 70, relativeY + 75, subheadingTextColor(), 0.8F);
    }
}
