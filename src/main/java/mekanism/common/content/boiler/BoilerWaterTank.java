package mekanism.common.content.boiler;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;

public class BoilerWaterTank extends BoilerTank {

    public BoilerWaterTank(TileEntityBoilerCasing tile) {
        super(tile);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        return multiblock.structure != null ? multiblock.structure.waterStored : FluidStack.EMPTY;
    }

    @Override
    public void setFluid(@Nonnull FluidStack stack) {
        if (multiblock.structure != null) {
            multiblock.structure.waterStored = stack;
        }
    }

    @Override
    public int getCapacity() {
        return multiblock.structure != null ? multiblock.structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK : 0;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return stack.getFluid().getTags().contains(FluidTags.WATER.getId());
    }
}