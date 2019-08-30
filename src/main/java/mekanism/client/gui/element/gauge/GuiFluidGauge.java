package mekanism.client.gui.element.gauge;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

@OnlyIn(Dist.CLIENT)
public class GuiFluidGauge extends GuiTankGauge<FluidStack, FluidTank> {

    public GuiFluidGauge(IFluidInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y, handler);
        //Ensure it isn't null
        setDummyType(FluidStack.EMPTY);
    }

    public static GuiFluidGauge getDummy(Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        GuiFluidGauge gauge = new GuiFluidGauge(null, type, gui, def, x, y);
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
        if (tank.getFluid().isEmpty() || tank.getCapacity() == 0) {
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
            return TextComponentUtil.translate("mekanism.gui.empty");
        }
        int amount = infoHandler.getTank().getFluidAmount();
        if (amount == Integer.MAX_VALUE) {
            return TextComponentUtil.translate("mekanism.gui.infinite");
        }
        return TextComponentUtil.build(fluidStack, ": " + amount + " mB");
    }

    @Override
    protected void applyRenderColor() {
        MekanismRenderer.color(dummy ? dummyType : infoHandler.getTank().getFluid());
    }

    public interface IFluidInfoHandler extends ITankInfoHandler<FluidTank> {

    }
}