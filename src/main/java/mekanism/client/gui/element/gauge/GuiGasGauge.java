package mekanism.client.gui.element.gauge;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGasGauge extends GuiTankGauge<Gas, GasTank> {

    public GuiGasGauge(IGasInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y, handler);
    }

    public static GuiGasGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, def, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.GAS;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        //TODO: Can capacity ever be zero when tank is not empty?
        if (infoHandler.getTank().isEmpty() || infoHandler.getTank().getCapacity() == 0) {
            return 0;
        }
        return infoHandler.getTank().getStored() * (height - 2) / infoHandler.getTank().getCapacity();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        return (infoHandler.getTank() != null && !infoHandler.getTank().isEmpty()) ? MekanismRenderer.getChemicalTexture(infoHandler.getTank().getType()) : null;
    }

    @Override
    public ITextComponent getTooltipText() {
        if (dummy) {
            return TextComponentUtil.build(dummyType);
        }
        if (infoHandler.getTank().isEmpty()) {
            return TextComponentUtil.translate("gui.mekanism.empty");
        }
        return TextComponentUtil.build(infoHandler.getTank().getStack(), ": " + infoHandler.getTank().getStored());
    }

    @Override
    protected void applyRenderColor() {
        if (dummy) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(infoHandler.getTank().getStack());
        }
    }

    public interface IGasInfoHandler extends ITankInfoHandler<GasTank> {
    }
}