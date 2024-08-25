package mekanism.common.capabilities.holder.fluid;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class FluidTankHelper {

    private final IFluidTankHolder slotHolder;
    private boolean built;

    private FluidTankHelper(IFluidTankHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static FluidTankHelper forSide(Supplier<Direction> facingSupplier) {
        return new FluidTankHelper(new FluidTankHolder(facingSupplier));
    }

    public static FluidTankHelper forSideWithConfig(ISideConfiguration sideConfiguration) {
        return new FluidTankHelper(new ConfigFluidTankHolder(sideConfiguration));
    }

    public <TANK extends IExtendedFluidTank> TANK addTank(@NotNull TANK tank) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof FluidTankHolder slotHolder) {
            slotHolder.addTank(tank);
        } else if (slotHolder instanceof ConfigFluidTankHolder slotHolder) {
            slotHolder.addTank(tank);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks");
        }
        return tank;
    }

    public <TANK extends IExtendedFluidTank> TANK addTank(@NotNull TANK tank, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof FluidTankHolder slotHolder) {
            slotHolder.addTank(tank, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks on specific sides");
        }
        return tank;
    }

    public IFluidTankHolder build() {
        built = true;
        return slotHolder;
    }
}