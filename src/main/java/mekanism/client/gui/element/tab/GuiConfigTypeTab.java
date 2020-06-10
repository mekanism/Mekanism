package mekanism.client.gui.element.tab;

import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.custom.GuiSideConfiguration;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiConfigTypeTab extends GuiInsetElement<TileEntity> {

    private final TransmissionType transmission;
    private final GuiSideConfiguration config;

    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, int x, int y, GuiSideConfiguration config, boolean left) {
        super(getResource(type), gui, null, x, y, 26, 18, left);
        this.config = config;
        transmission = type;
    }

    private static ResourceLocation getResource(TransmissionType t) {
        return MekanismUtils.getResource(ResourceType.GUI, t.getTransmission() + ".png");
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
            case INFUSION:
                MekanismRenderer.color(EnumColor.DARK_RED);
                break;
            case PIGMENT:
                MekanismRenderer.color(EnumColor.PINK);
                break;
            case SLURRY:
                MekanismRenderer.color(EnumColor.BROWN);
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
        config.setCurrentType(transmission);
        config.updateTabs();
    }
}