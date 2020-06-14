package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationValve() {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return side -> getMultiblock().getInventorySlots(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID || type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Nonnull
    @Override
    public FluidStack insertFluid(@Nonnull FluidStack stack, Direction side, @Nonnull Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            getMultiblock().triggerValveTransfer(this);
        }
        return ret;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}