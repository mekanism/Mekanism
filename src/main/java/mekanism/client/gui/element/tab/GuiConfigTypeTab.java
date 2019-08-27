package mekanism.client.gui.element.tab;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiConfigTypeTab extends GuiElement {

    private final TransmissionType transmission;
    private boolean left;

    //TODO: Convert this to properly using x and y
    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, ResourceLocation def) {
        super(getResource(type), gui, def, 0, 0, 26, 26);
        transmission = type;
        //TODO: x = getLeftBound(false) - 4;
    }

    private static ResourceLocation getResource(TransmissionType t) {
        return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, t.getTransmission() + ".png");
    }

    public void setYOffset(int yOffset) {
        y = guiObj.getTop() + yOffset;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public TransmissionType getTransmissionType() {
        return transmission;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(getLeftBound(false) - 4, y, 0, left ? 0 : 26, 26, 26);
        guiObj.drawTexturedRect(getLeftBound(true), y + 4, 26, isMouseOver(mouseX, mouseY) ? 0 : 18, 18, 18);
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(TextComponentUtil.build(transmission), mouseX, mouseY);
    }

    public int getLeftBound(boolean adjust) {
        return left ? -21 + (adjust ? 1 : 0) : 179 - (adjust ? 1 : 0);
    }

    public int getRightBound(boolean adjust) {
        return left ? -3 + (adjust ? 1 : 0) : 197 - (adjust ? 1 : 0);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ((GuiSideConfiguration) guiObj).setCurrentType(transmission);
        ((GuiSideConfiguration) guiObj).updateTabs();
    }
}