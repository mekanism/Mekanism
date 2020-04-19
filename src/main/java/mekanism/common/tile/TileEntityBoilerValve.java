package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

    public TileEntityBoilerValve() {
        super(MekanismBlocks.BOILER_VALVE);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getGasTanks(side);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> structure == null ? Collections.emptyList() : structure.getFluidTanks(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            GasUtils.emit(structure.steamTank, this);
        }
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle fluid or gas when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID || type == SubstanceType.GAS) {
            return false;
        }
        return super.persists(type);
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.waterTank.getFluidAmount(), structure.waterTank.getCapacity());
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Direction side, Action action) {
        FluidStack ret = super.insertFluid(stack, side, action);
        if (ret.getAmount() < stack.getAmount() && action.execute()) {
            if (structure != null) {
                Coord4D coord4D = Coord4D.get(this);
                for (ValveData data : structure.valves) {
                    if (coord4D.equals(data.location)) {
                        data.onTransfer();
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY && structure != null &&
            (structure.upperRenderLocation == null || getPos().getY() < structure.upperRenderLocation.y - 1)) {
            //If we are a valve for water, then disable the gas handler capability
            return true;
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && structure != null && structure.upperRenderLocation != null &&
                   getPos().getY() >= structure.upperRenderLocation.y - 1) {
            //If we are a valve for steam, then disable the fluid handler capability
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}