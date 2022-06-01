package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock {

    public TileEntityThermalEvaporationValve(BlockPos pos, BlockState state) {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE, pos, state);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
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