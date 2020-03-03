package mekanism.generators.common.tile.turbine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityTurbineValve extends TileEntityTurbineCasing implements IEnergyWrapper, IComputerIntegration {

    private static final String[] methods = new String[]{"isFormed", "getSteam", "getFlowRate", "getMaxFlow", "getSteamInput"};
    private CapabilityWrapperManager<IEnergyWrapper, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(IEnergyWrapper.class, ForgeEnergyIntegration.class);

    public TileEntityTurbineValve() {
        super(GeneratorsBlocks.TURBINE_VALVE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            if (structure != null) {
                CableUtils.emit(this);
            }
        }
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return structure != null && !structure.locations.contains(Coord4D.get(this).offset(side));
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return false;
    }

    @Override
    public double getMaxOutput() {
        return structure != null ? structure.getEnergyCapacity() : 0;
    }

    @Override
    public double acceptEnergy(Direction side, double amount, boolean simulate) {
        return 0;
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        double toGive = Math.min(getEnergy(), amount);
        if (toGive < 0.0001 || (side != null && !canOutputEnergy(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }
        return toGive;
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

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        if (!canHandleFluid() || structure == null) {
            return Collections.emptyList();
        }
        return structure.getFluidTanks(side);
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        if (method == 0) {
            return new Object[]{structure != null};
        } else {
            if (structure == null) {
                return new Object[]{"Unformed"};
            }
            switch (method) {
                case 1:
                    return new Object[]{structure.fluidTank.getFluidAmount()};
                case 2:
                    return new Object[]{structure.clientFlow};
                case 3:
                    double rate = structure.lowerVolume * (structure.clientDispersers * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get());
                    rate = Math.min(rate, structure.vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
                    return new Object[]{rate};
                case 4:
                    return new Object[]{structure.lastSteamInput};
            }
        }
        throw new NoSuchMethodException();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
                return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
            }
            if (capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
                return Capabilities.ENERGY_OUTPUTTER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
            }
            if (capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, getDirection())));
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.fluidTank.getFluidAmount(), structure.fluidTank.getCapacity());
    }
}