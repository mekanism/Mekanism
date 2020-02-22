package mekanism.common.content.boiler;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;

public class BoilerWaterTank extends BoilerTank {

    public BoilerWaterTank(TileEntityBoilerCasing tile) {
        super(tile);
    }

    @Override
    public int getCapacity() {
        return multiblock.structure == null ? 0 : multiblock.structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return super.isFluidValid(stack) && stack.getFluid().isIn(FluidTags.WATER);
    }
}