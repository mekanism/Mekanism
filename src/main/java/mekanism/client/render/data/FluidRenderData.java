package mekanism.client.render.data;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
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
        return fluidType.getFluid().getAttributes().isGaseous(fluidType);
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
        return MekanismRenderer.getFluidTexture(fluidType, FluidType.STILL);
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        code = 31 * code + fluidType.getFluid().getRegistryName().hashCode();
        if (fluidType.hasTag()) {
            code = 31 * code + fluidType.getTag().hashCode();
        }
        return code;
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof FluidRenderData && fluidType.isFluidEqual(((FluidRenderData) data).fluidType);
    }
}