package mekanism.client.gui.element.custom.module;

import mekanism.client.gui.element.button.RadioButton;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

class BooleanToggle extends MiniElement {

    private static final int RADIO_SIZE = RadioButton.RADIO_SIZE;

    private final ModuleConfigItem<Boolean> data;

    BooleanToggle(GuiModuleScreen parent, ModuleConfigItem<Boolean> data, int xPos, int yPos, int dataIndex) {
        super(parent, xPos, yPos, dataIndex);
        this.data = data;
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
        parent.drawTextWithScale(guiGraphics, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
        parent.drawTextWithScale(guiGraphics, MekanismLang.TRUE.translate(), getRelativeX() + 16, getRelativeY() + 11, textColor, 0.8F);
        parent.drawTextWithScale(guiGraphics, MekanismLang.FALSE.translate(), getRelativeX() + 62, getRelativeY() + 11, textColor, 0.8F);
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
        setData(data, value);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
    }
}