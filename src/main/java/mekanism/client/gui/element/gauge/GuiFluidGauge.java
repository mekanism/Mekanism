package mekanism.client.gui.element.gauge;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiFluidGauge extends GuiTankGauge<FluidStack, FluidTank> {

    public GuiFluidGauge(IFluidInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y, handler);
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
        if (infoHandler.getTank().getFluid() == null || infoHandler.getTank().getCapacity() == 0) {
            return 0;
        }
        if (infoHandler.getTank().getFluidAmount() == Integer.MAX_VALUE) {
            return height - 2;
        }
        return infoHandler.getTank().getFluidAmount() * (height - 2) / infoHandler.getTank().getCapacity();
    }

    @Override
    public TextureAtlasSprite getIcon() {
        if (dummy) {
            return MekanismRenderer.getFluidTexture(dummyType, FluidType.STILL);
        }
        FluidStack fluid = infoHandler.getTank().getFluid();
        return MekanismRenderer.getFluidTexture(fluid == null ? dummyType : fluid, FluidType.STILL);
    }

    @Override
    public String getTooltipText() {
        if (dummy) {
            return dummyType.getLocalizedName();
        }
        FluidTank tank = infoHandler.getTank();
        String amountStr = tank.getFluidAmount() == Integer.MAX_VALUE ? LangUtils.localize("gui.infinite") : tank.getFluidAmount() + " mB";
        return tank.getFluid() != null ? LangUtils.localizeFluidStack(tank.getFluid()) + ": " + amountStr : LangUtils.localize("gui.empty");
    }

    @Override
    protected void applyRenderColor() {
        MekanismRenderer.color(dummy ? dummyType : infoHandler.getTank().getFluid());
    }

    public interface IFluidInfoHandler extends ITankInfoHandler<FluidTank> {

    }
}