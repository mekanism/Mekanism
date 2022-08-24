package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.client.render.data.ChemicalRenderData.GasRenderData;
import mekanism.client.render.data.ChemicalRenderData.InfusionRenderData;
import mekanism.client.render.data.ChemicalRenderData.PigmentRenderData;
import mekanism.client.render.data.ChemicalRenderData.SlurryRenderData;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
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

    public abstract TextureAtlasSprite getTexture();

    public abstract boolean isGaseous();

    public abstract int getColorARGB(float scale);

    public int calculateGlowLight(int light) {
        return light;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, length, width);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof RenderData data && data.height == height && data.length == length && data.width == width;
    }

    public static class Builder<DATA_TYPE extends RenderData> {

        @Nullable
        private final Chemical<?> chemical;
        private final FluidStack fluid;
        @Nullable
        private BlockPos location;
        private int height;
        private int length;
        private int width;

        private Builder(@Nullable Chemical<?> chemical, FluidStack fluid) {
            this.chemical = chemical;
            this.fluid = fluid;
        }

        public static <CHEMICAL extends Chemical<CHEMICAL>> Builder<ChemicalRenderData<CHEMICAL>> create(ChemicalStack<CHEMICAL> chemical) {
            if (chemical.isEmpty()) {
                throw new IllegalArgumentException("Chemical may not be empty");
            }
            return new Builder<>(chemical.getType(), FluidStack.EMPTY);
        }

        public static Builder<FluidRenderData> create(FluidStack fluid) {
            if (fluid.isEmpty()) {
                throw new IllegalArgumentException("Fluid may not be empty");
            }
            return new Builder<>(null, fluid);
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
            return location(Objects.requireNonNull(multiblock.renderLocation, "Render location may not be null."))
                  .height(multiblock.height() - 2)
                  .length(multiblock.length())
                  .width(multiblock.width());
        }

        public DATA_TYPE build() {
            if (location == null) {
                throw new IllegalStateException("Incomplete render data builder, no render location set.");
            }
            RenderData data;
            if (!fluid.isEmpty()) {
                data = new FluidRenderData(location, width, height, length, fluid);
            } else if (chemical instanceof Gas gas) {
                data = new GasRenderData(location, width, height, length, gas);
            } else if (chemical instanceof InfuseType infuseType) {
                data = new InfusionRenderData(location, width, height, length, infuseType);
            } else if (chemical instanceof Pigment pigment) {
                data = new PigmentRenderData(location, width, height, length, pigment);
            } else if (chemical instanceof Slurry slurry) {
                data = new SlurryRenderData(location, width, height, length, slurry);
            } else {
                throw new IllegalStateException("Incomplete render data builder, missing or unknown chemical or fluid.");
            }
            //noinspection unchecked
            return (DATA_TYPE) data;
        }
    }
}