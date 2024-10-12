package mekanism.client.gui.element.custom.module;

import mekanism.api.gear.config.ModuleBooleanConfig;
import mekanism.client.gui.element.button.RadioButton;
import mekanism.client.gui.element.scroll.GuiScrollList;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;

class BooleanToggle extends MiniElement<Boolean> {

    private static final int RADIO_SIZE = RadioButton.RADIO_SIZE;

    BooleanToggle(GuiModuleScreen parent, ModuleBooleanConfig data, Component description, int xPos, int yPos) {
        super(parent, data, description, xPos, yPos);
    }

    @Override
    protected int getNeededHeight() {
        return 20;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawRadio(guiGraphics, mouseX, mouseY, data.get(), 4, 11, 0);
        drawRadio(guiGraphics, mouseX, mouseY, !data.get(), 50, 11, RADIO_SIZE);
    }

    private void drawRadio(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean selected, int relativeX, int relativeY, int selectedU) {
        if (selected) {
            guiGraphics.blit(RadioButton.RADIO, getRelativeX() + relativeX, getRelativeY() + relativeY, selectedU, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE, 2 * RADIO_SIZE, 2 * RADIO_SIZE);
        } else {
            boolean hovered = mouseOver(mouseX, mouseY, relativeX, relativeY, RADIO_SIZE, RADIO_SIZE);
            guiGraphics.blit(RadioButton.RADIO, getRelativeX() + relativeX, getRelativeY() + relativeY, hovered ? RADIO_SIZE : 0, 0, RADIO_SIZE, RADIO_SIZE, 2 * RADIO_SIZE, 2 * RADIO_SIZE);
        }
    }

    @Override
    protected void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        parent.drawScaledScrollingString(guiGraphics, description, xPos, yPos, TextAlignment.LEFT, textColor, parent.getScreenWidth() - GuiScrollList.TEXTURE_WIDTH,
              2, false, 0.8F);

        int trueShift = 4 + RADIO_SIZE;
        int falseShift = 50 + RADIO_SIZE;
        parent.drawScaledScrollingString(guiGraphics, MekanismLang.TRUE.translate(), xPos + trueShift, yPos + 11, TextAlignment.LEFT, textColor,
              50 - trueShift, 3, false, 0.8F);
        parent.drawScaledScrollingString(guiGraphics, MekanismLang.FALSE.translate(), xPos + falseShift, yPos + 11, TextAlignment.LEFT, textColor,
              parent.getScreenWidth() - GuiScrollList.TEXTURE_WIDTH - falseShift, 3, false, 0.8F);
    }

    @Override
    protected void click(double mouseX, double mouseY) {
        if (data.get()) {
            if (mouseOver(mouseX, mouseY, 50, 11, RADIO_SIZE, RADIO_SIZE)) {
                setDataFromClick(false);
            }
        } else if (mouseOver(mouseX, mouseY, 4, 11, RADIO_SIZE, RADIO_SIZE)) {
            setDataFromClick(true);
        }
    }

    private void setDataFromClick(boolean value) {
        setData(value);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(value ? MekanismSounds.BEEP_ON.get() : MekanismSounds.BEEP_OFF.get(), 1.0F, 1.0F));
    }
}