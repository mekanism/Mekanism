package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiConfigTypeTab extends GuiInsetElement<Void> {

    private final TransmissionType transmission;
    private final GuiSideConfiguration<?> config;

    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, int x, int y, GuiSideConfiguration<?> config, boolean left) {
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
                MekanismRenderer.color(SpecialColors.TAB_ENERGY_CONFIG);
                break;
            case FLUID:
                MekanismRenderer.color(SpecialColors.TAB_FLUID_CONFIG);
                break;
            case GAS:
                MekanismRenderer.color(SpecialColors.TAB_GAS_CONFIG);
                break;
            case INFUSION:
                MekanismRenderer.color(SpecialColors.TAB_INFUSION_CONFIG);
                break;
            case PIGMENT:
                MekanismRenderer.color(SpecialColors.TAB_PIGMENT_CONFIG);
                break;
            case SLURRY:
                MekanismRenderer.color(SpecialColors.TAB_SLURRY_CONFIG);
                break;
            case ITEM:
                MekanismRenderer.color(SpecialColors.TAB_ITEM_CONFIG);
                break;
            case HEAT:
                MekanismRenderer.color(SpecialColors.TAB_HEAT_CONFIG);
                break;
        }
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, TextComponentUtil.translate(transmission.getTranslationKey()), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        config.setCurrentType(transmission);
        config.updateTabs();
    }
}