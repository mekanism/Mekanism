package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityBoilerValve extends TileEntityBoilerCasing {

    public TileEntityBoilerValve() {
        super(MekanismBlocks.BOILER_VALVE);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
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
    public boolean persistFluid() {
        //Do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Override
    public boolean persistGas() {
        //Do not handle gas when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.waterTank.getFluidAmount(), structure.waterTank.getCapacity());
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