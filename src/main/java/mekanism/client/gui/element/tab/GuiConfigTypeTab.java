package mekanism.client.gui.element.tab;

import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.GuiSideConfiguration;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiConfigTypeTab extends GuiInsetElement<TileEntity> {

    private final TransmissionType transmission;

    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, ResourceLocation def, int x, int y) {
        super(getResource(type), gui, def, null, x, y, 26, 18);
        transmission = type;
    }

    private static ResourceLocation getResource(TransmissionType t) {
        return MekanismUtils.getResource(ResourceType.GUI_ELEMENT, t.getTransmission() + ".png");
    }

    public TransmissionType getTransmissionType() {
        return transmission;
    }

    @Override
    protected void colorTab() {
        switch (transmission) {
            case ENERGY:
                MekanismRenderer.color(EnumColor.DARK_GREEN);
                break;
            case FLUID:
                MekanismRenderer.color(EnumColor.DARK_BLUE);
                break;
            case GAS:
                MekanismRenderer.color(EnumColor.YELLOW);
                break;
            case ITEM:
                break;
            case HEAT:
                MekanismRenderer.color(EnumColor.ORANGE);
                break;
        }
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(TextComponentUtil.translate(transmission.getTranslationKey()), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ((GuiSideConfiguration) guiObj).setCurrentType(transmission);
        ((GuiSideConfiguration) guiObj).updateTabs();
    }
}