package mekanism.generators.common.content.turbine;

import javax.annotation.Nonnull;
import mekanism.common.base.MultiblockFluidTank;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraftforge.fluids.FluidStack;

public class TurbineFluidTank extends MultiblockFluidTank<TileEntityTurbineCasing> {

    public TurbineFluidTank(TileEntityTurbineCasing tileEntity) {
        super(tileEntity);
    }

    @Override
    @Nonnull
    public FluidStack getFluid() {
        return multiblock.structure != null ? multiblock.structure.fluidStored : FluidStack.EMPTY;
    }

    @Override
    public void setFluid(@Nonnull FluidStack stack) {
        if (multiblock.structure != null) {
            multiblock.structure.fluidStored = stack;
        }
    }

    @Override
    public int getCapacity() {
        return multiblock.structure != null ? multiblock.structure.getFluidCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        return getFluid().isEmpty() || getFluid().isFluidEqual(stack);
    }

    @Override
    protected void updateValveData() {
    }
}