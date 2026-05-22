package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.client.render.ModelRenderer;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class RenderData {

    public final BlockPos location;
    public final int height;
    public final int length;
    public final int width;

    protected RenderData(BlockPos renderLocation, int width, int height, int length) {
        this.location = renderLocation;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public abstract boolean isGaseous();

    public abstract int getColorARGB(float scale);

    public int calculateGlowLight(int light) {
        return light;
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + height;
        result = 31 * result + length;
        result = 31 * result + width;
        return result;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return object instanceof RenderData data && data.height == height && data.length == length && data.width == width;
    }

    public static class Builder<DATA_TYPE extends RenderData> {

        private final ChemicalResource chemical;
        private final FluidResource fluid;
        @Nullable
        private BlockPos location;
        private int height;
        private int length;
        private int width;

        private Builder(ChemicalResource chemical, FluidResource fluid) {
            this.chemical = chemical;
            this.fluid = fluid;
        }

        public static Builder<ChemicalRenderData> create(IChemicalTank tank) {
            return create(tank.resource());
        }

        public static Builder<ChemicalRenderData> create(ChemicalResource chemical) {
            if (chemical.isEmpty()) {
                throw new IllegalArgumentException("Chemical may not be empty");
            }
            return new Builder<>(chemical, FluidResource.EMPTY);
        }

        public static Builder<FluidRenderData> create(IFluidTank tank) {
            return create(tank.resource());
        }

        public static Builder<FluidRenderData> create(FluidResource fluid) {
            if (fluid.isEmpty()) {
                throw new IllegalArgumentException("Fluid may not be empty");
            }
            return new Builder<>(ChemicalResource.EMPTY, fluid);
        }

        public Builder<DATA_TYPE> location(BlockPos renderLocation) {
            this.location = renderLocation;
            return this;
        }

        public Builder<DATA_TYPE> height(int height) {
            this.height = height;
            return this;
        }

        public Builder<DATA_TYPE> length(int length) {
            this.length = length;
            return this;
        }

        public Builder<DATA_TYPE> width(int width) {
            this.width = width;
            return this;
        }

        public Builder<DATA_TYPE> dimensions(int width, int height, int length) {
            return width(width).height(height).length(length);
        }

        public Builder<DATA_TYPE> of(MultiblockData multiblock) {
            return location(Objects.requireNonNull(multiblock.renderLocation, "Render location may not be null.").offset(1, 0, 1))
                  .dimensions(multiblock.width() - 2, multiblock.height() - 2, multiblock.length() - 2);
        }

        public DATA_TYPE build() {
            if (location == null) {
                throw new IllegalStateException("Incomplete render data builder, no render location set.");
            }
            RenderData data;
            if (!fluid.isEmpty()) {
                data = new FluidRenderData(location, width, height, length, fluid);
            } else if (!chemical.isEmpty()) {
                data = new ChemicalRenderData(location, width, height, length, chemical);
            } else {
                throw new IllegalStateException("Incomplete render data builder, missing or unknown chemical or fluid.");
            }
            //noinspection unchecked
            return (DATA_TYPE) data;
        }

        public ScaledRenderData buildScaled(float scale) {
            if (location == null) {
                throw new IllegalStateException("Incomplete render data builder, no render location set.");
            }
            ScaledRenderData data;
            if (!fluid.isEmpty()) {
                data = new FluidRenderData.Scaled(location, width, height, length, fluid, scale);
            } else if (!chemical.isEmpty()) {
                data = new ChemicalRenderData.Scaled(location, width, height, length, chemical, scale);
            } else {
                throw new IllegalStateException("Incomplete render data builder, missing or unknown chemical or fluid.");
            }
            return data;
        }
    }

    public interface ScaledRenderData {

        default RenderData asRenderData() {
            return (RenderData) this;
        }

        /**
         * @return the scale to use on {@link ModelRenderer#getModel(RenderData, double)}
         */
        float scale();
    }
}