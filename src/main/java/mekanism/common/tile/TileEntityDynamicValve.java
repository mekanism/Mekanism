package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityDynamicValve extends TileEntityDynamicTank {

    public TileEntityDynamicValve() {
        super(MekanismBlocks.DYNAMIC_VALVE);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getMultiblock().getFluidTanks(side);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return side -> getMultiblock().getGasTanks(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID || type == SubstanceType.GAS) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Direction side, Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            if (getMultiblock().isFormed()) {
                Coord4D coord4D = Coord4D.get(this);
                for (ValveData data : getMultiblock().valves) {
                    if (coord4D.equals(data.location)) {
                        data.onTransfer();
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public int getRedstoneLevel() {
        return getMultiblock().getCurrentRedstoneLevel();
    }
}