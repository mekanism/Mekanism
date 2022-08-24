package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidRenderData extends RenderData {

    public final FluidStack fluidType;

    public FluidRenderData(BlockPos renderLocation, int width, int height, int length, FluidStack fluidType) {
        super(renderLocation, width, height, length);
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
        return Objects.hash(super.hashCode(), fluidType.getFluid(), fluidType.getTag());
    }

    @Override
    public boolean equals(Object data) {
        return super.equals(data) && data instanceof FluidRenderData other && fluidType.isFluidEqual(other.fluidType);
    }
}