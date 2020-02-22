package mekanism.client.gui.element.gauge;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiGasGauge extends GuiTankGauge<Gas, GasTank> {

    public GuiGasGauge(IGasInfoHandler handler, GaugeType type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y, handler);
    }

    public static GuiGasGauge getDummy(GaugeType type, IGuiWrapper gui, int x, int y) {
        GuiGasGauge gauge = new GuiGasGauge(null, type, gui, x, y);
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
        GasTank tank = infoHandler.getTank();
        if (tank == null || tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        return tank.getStored() * (height - 2) / tank.getCapacity();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getChemicalTexture(dummyType);
        }
        return infoHandler.getTank() == null || infoHandler.getTank().isEmpty() ? null : MekanismRenderer.getChemicalTexture(infoHandler.getTank().getType());
    }

    @Override
    public ITextComponent getTooltipText() {
        if (dummy) {
            return TextComponentUtil.build(dummyType);
        }
        GasTank tank = infoHandler.getTank();
        if (tank == null || tank.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        }
        int amount = tank.getStored();
        if (amount == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(tank.getType(), MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(tank.getType(), amount);
    }

    @Override
    protected void applyRenderColor() {
        if (dummy || infoHandler.getTank() == null) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(infoHandler.getTank().getStack());
        }
    }

    public interface IGasInfoHandler extends ITankInfoHandler<GasTank> {
    }
}