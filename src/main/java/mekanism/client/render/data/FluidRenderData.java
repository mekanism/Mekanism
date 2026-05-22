package mekanism.client.render.data;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidRenderData extends RenderData {

    public final FluidResource fluidType;

    public FluidRenderData(BlockPos renderLocation, int width, int height, int length, FluidResource fluidType) {
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
    public int hashCode() {
        return 31 * super.hashCode() + fluidType.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object data) {
        if (data == this) {
            return true;
        } else if (data == null) {
            return false;
        }
        return data.getClass() == FluidRenderData.class && equalsCommonFluid(data);
    }

    protected boolean equalsCommonFluid(Object data) {
        return super.equals(data) && fluidType.equals(((FluidRenderData) data).fluidType);
    }

    public static class Scaled extends FluidRenderData implements ScaledRenderData {

        private final float scale;

        public Scaled(BlockPos renderLocation, int width, int height, int length, FluidResource fluidType, float scale) {
            super(renderLocation, width, height, length, fluidType);
            this.scale = scale;
        }

        @Override
        public float scale() {
            return scale;
        }

        @Override
        public boolean equals(@Nullable Object data) {
            if (data == this) {
                return true;
            } else if (data == null) {
                return false;
            }
            return data.getClass() == Scaled.class && equalsCommonFluid(data) && scale == ((Scaled) data).scale;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Float.hashCode(scale);
        }
    }
}