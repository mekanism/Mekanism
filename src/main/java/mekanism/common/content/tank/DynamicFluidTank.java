package mekanism.common.content.tank;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.base.MultiblockFluidTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraftforge.fluids.FluidStack;

public class DynamicFluidTank extends MultiblockFluidTank<TileEntityDynamicTank> {

    public DynamicFluidTank(TileEntityDynamicTank tile) {
        super(tile);
    }

    @Nonnull
    @Override
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
        return multiblock.structure != null ? multiblock.structure.volume * TankUpdateProtocol.FLUID_PER_TANK : 0;
    }

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        if (multiblock.structure == null) {
            return false;
        }
        return multiblock.structure.fluidStored.isEmpty() || multiblock.structure.fluidStored.isFluidEqual(stack);
    }

    @Override
    protected void updateValveData() {
        if (multiblock.structure != null) {
            Coord4D coord4D = Coord4D.get(multiblock);
            for (ValveData data : multiblock.structure.valves) {
                if (coord4D.equals(data.location)) {
                    data.onTransfer();
                }
            }
        }
    }
}