package mekanism.common.capabilities.holder.fluid;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class FluidTankHelper {

    private final IFluidTankHolder slotHolder;
    private boolean built;

    private FluidTankHelper(IFluidTankHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static FluidTankHelper forSide(Supplier<Direction> facingSupplier) {
        return new FluidTankHelper(new FluidTankHolder(facingSupplier));
    }

    public static FluidTankHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new FluidTankHelper(new ConfigFluidTankHolder(facingSupplier, configSupplier));
    }

    public void addTank(@Nonnull IExtendedFluidTank tank) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof FluidTankHolder) {
            ((FluidTankHolder) slotHolder).addTank(tank);
        } else if (slotHolder instanceof ConfigFluidTankHolder) {
            ((ConfigFluidTankHolder) slotHolder).addTank(tank);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks");
        }
    }

    public void addTank(@Nonnull IExtendedFluidTank tank, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof FluidTankHolder) {
            ((FluidTankHolder) slotHolder).addTank(tank, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add tanks on specific sides");
        }
    }

    public IFluidTankHolder build() {
        built = true;
        return slotHolder;
    }
}