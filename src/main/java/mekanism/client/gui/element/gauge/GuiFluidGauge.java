package mekanism.client.gui.element.gauge;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

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
    public ITextComponent getTooltipText() {
        if (dummy) {
            return TextComponentUtil.build(dummyType);
        }
        FluidStack fluidStack = infoHandler.getTank().getFluid();
        if (fluidStack != null) {
            int amount = infoHandler.getTank().getFluidAmount();
            if (amount == Integer.MAX_VALUE) {
                return TextComponentUtil.build(Translation.of("mekanism.gui.infinite"));
            }
            return TextComponentUtil.build(fluidStack, ": " + amount);
        }
        return TextComponentUtil.build(Translation.of("mekanism.gui.empty"));
    }

    @Override
    protected void applyRenderColor() {
        MekanismRenderer.color(dummy ? dummyType : infoHandler.getTank().getFluid());
    }

    public interface IFluidInfoHandler extends ITankInfoHandler<FluidTank> {

    }
}