package mekanism.client.gui.element.gauge;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class GuiFluidGauge extends GuiTankGauge<FluidStack, FluidTank> {

    public GuiFluidGauge(IFluidInfoHandler handler, Type type, IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y, handler);
        //Ensure it isn't null
        setDummyType(FluidStack.EMPTY);
    }

    public static GuiFluidGauge getDummy(Type type, IGuiWrapper gui, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, x, y);
        gauge.dummy = true;
        return gauge;
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.FLUID;
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        FluidTank tank = infoHandler.getTank();
        if (tank.isEmpty() || tank.getCapacity() == 0) {
            return 0;
        }
        if (tank.getFluidAmount() == Integer.MAX_VALUE) {
            return height - 2;
        }
        return tank.getFluidAmount() * (height - 2) / tank.getCapacity();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getFluidTexture(dummyType, FluidType.STILL);
        }
        FluidStack fluid = infoHandler.getTank().getFluid();
        return MekanismRenderer.getFluidTexture(fluid.isEmpty() ? dummyType : fluid, FluidType.STILL);
    }

    @Override
    public ITextComponent getTooltipText() {
        if (dummy) {
            return TextComponentUtil.build(dummyType);
        }
        FluidStack fluidStack = infoHandler.getTank().getFluid();
        if (fluidStack.isEmpty()) {
            return MekanismLang.EMPTY.translate();
        }
        int amount = infoHandler.getTank().getFluidAmount();
        if (amount == Integer.MAX_VALUE) {
            return MekanismLang.GENERIC_STORED.translate(fluidStack, MekanismLang.INFINITE);
        }
        return MekanismLang.GENERIC_STORED_MB.translate(fluidStack, amount);
    }

    @Override
    protected void applyRenderColor() {
        MekanismRenderer.color(dummy ? dummyType : infoHandler.getTank().getFluid());
    }

    public interface IFluidInfoHandler extends ITankInfoHandler<FluidTank> {

    }
}