package mekanism.client.gui.element.custom.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;

class BooleanToggle extends MiniElement {

    private static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    private static final int RADIO_SIZE = 8;

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
    protected void renderBackground(PoseStack matrix, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, RADIO);
        drawRadio(matrix, mouseX, mouseY, data.get(), 4, 11, 0);
        drawRadio(matrix, mouseX, mouseY, !data.get(), 50, 11, RADIO_SIZE);
    }

    private void drawRadio(PoseStack matrix, int mouseX, int mouseY, boolean selected, int relativeX, int relativeY, int selectedU) {
        if (selected) {
            GuiComponent.blit(matrix, getX() + relativeX, getY() + relativeY, selectedU, RADIO_SIZE, RADIO_SIZE, RADIO_SIZE, 16, 16);
        } else {
            boolean hovered = mouseOver(mouseX, mouseY, relativeX, relativeY, RADIO_SIZE, RADIO_SIZE);
            GuiComponent.blit(matrix, getX() + relativeX, getY() + relativeY, hovered ? RADIO_SIZE : 0, 0, RADIO_SIZE, RADIO_SIZE, 16, 16);
        }
    }

    @Override
    protected void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        int textColor = parent.screenTextColor();
        parent.drawTextWithScale(matrix, data.getDescription(), getRelativeX() + 3, getRelativeY(), textColor, 0.8F);
        parent.drawTextWithScale(matrix, MekanismLang.TRUE.translate(), getRelativeX() + 16, getRelativeY() + 11, textColor, 0.8F);
        parent.drawTextWithScale(matrix, MekanismLang.FALSE.translate(), getRelativeX() + 62, getRelativeY() + 11, textColor, 0.8F);
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
        GuiElement.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
    }
}