package mekanism.client.gui.element.gauge;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
        if (infoHandler.getTank().getGas() == null || infoHandler.getTank().getMaxGas() == 0) {
            return 0;
        }
        return infoHandler.getTank().getStored() * (height - 2) / infoHandler.getTank().getMaxGas();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return dummyType.getSprite();
        }
        return (infoHandler.getTank() != null && infoHandler.getTank().getGas() != null && infoHandler.getTank().getGas().getGas() != null) ?
               infoHandler.getTank().getGas().getGas().getSprite() : null;
    }

    @Override
    public String getTooltipText() {
        if (dummy) {
            return dummyType.getLocalizedName();
        }
        return (infoHandler.getTank().getGas() != null) ? infoHandler.getTank().getGas().getGas().getLocalizedName() + ": " + infoHandler.getTank().getStored()
                                                        : LangUtils.localize("gui.empty");
    }

    @Override
    protected void applyRenderColor() {
        if (dummy) {
            MekanismRenderer.color(dummyType);
        } else {
            MekanismRenderer.color(infoHandler.getTank().getGas());
        }
    }

    public interface IGasInfoHandler extends ITankInfoHandler<GasTank> {
    }
}