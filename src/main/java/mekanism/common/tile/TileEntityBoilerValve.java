package mekanism.common.tile;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.Capabilities;
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

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && structure.upperRenderLocation != null && getPos().getY() >= structure.upperRenderLocation.y - 1) {
            GasUtils.emit(structure.steamTank, this);
        }
    }

    @Override
    public boolean canHandleFluid() {
        //Mark that we can handle fluid
        return true;
    }

    @Override
    public boolean persistFluid() {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Override
    public boolean canHandleGas() {
        //Mark that we can handle gas
        return true;
    }

    @Override
    public boolean persistGas() {
        //But that we do not handle gas when it comes to syncing it/saving this tile to disk
        return false;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return canHandleFluid() && structure != null ? structure.getFluidTanks(side) : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return canHandleFluid() && structure != null ? structure.getGasTanks(side) : Collections.emptyList();
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