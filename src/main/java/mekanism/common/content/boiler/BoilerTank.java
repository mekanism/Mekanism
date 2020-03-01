package mekanism.common.content.boiler;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import mekanism.api.Coord4D;
import mekanism.api.annotations.NonNull;
import mekanism.common.base.MultiblockFluidTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityBoilerCasing;
import net.minecraftforge.fluids.FluidStack;

public class BoilerTank extends MultiblockFluidTank<TileEntityBoilerCasing> {

    public static BoilerTank create(TileEntityBoilerCasing tile, IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        //TODO: Validate capacity is positive
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(validator, "Fluid validity check cannot be null");
        return new BoilerTank(tile, capacity, validator);
    }

    private BoilerTank(TileEntityBoilerCasing tile, IntSupplier capacity, Predicate<@NonNull FluidStack> validator) {
        super(tile, capacity, validator);
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