package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderData extends RenderData {

    @Nonnull
    public final FluidStack fluidType;

    public FluidRenderData(@Nonnull FluidStack fluidType) {
        this.fluidType = fluidType;
    }

    @Override
    public boolean isGaseous() {
        return MekanismUtils.lighterThanAirGas(fluidType);
    }

    public int getColorARGB() {
        return MekanismRenderer.getColorARGB(fluidType);
    }

    @Override
    public int getColorARGB(float scale) {
        return MekanismRenderer.getColorARGB(fluidType, scale);
    }

    @Override
    public int calculateGlowLight(int light) {
        return MekanismRenderer.calculateGlowLight(light, fluidType);
    }

    @Override
    public TextureAtlasSprite getTexture() {
        return MekanismRenderer.getFluidTexture(fluidType, FluidTextureType.STILL);
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + fluidType.getFluid().hashCode();
        if (fluidType.hasTag()) {
            code = 31 * code + fluidType.getTag().hashCode();
        }
        return code;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof FluidRenderData other && fluidType.isFluidEqual(other.fluidType);
    }
}