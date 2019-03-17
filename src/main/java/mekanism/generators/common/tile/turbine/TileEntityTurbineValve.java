package mekanism.generators.common.tile.turbine;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.CableUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.content.turbine.TurbineFluidTank;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional.Method;

public class TileEntityTurbineValve extends TileEntityTurbineCasing implements IFluidHandlerWrapper, IEnergyWrapper,
      IComputerIntegration {

    private static final String[] methods = new String[]{"isFormed", "getSteam", "getFlowRate", "getMaxFlow",
          "getSteamInput"};
    public boolean ic2Registered = false;
    public TurbineFluidTank fluidTank;
    private CapabilityWrapperManager<IEnergyWrapper, TeslaIntegration> teslaManager = new CapabilityWrapperManager<>(
          IEnergyWrapper.class, TeslaIntegration.class);
    private CapabilityWrapperManager<IEnergyWrapper, ForgeEnergyIntegration> forgeEnergyManager = new CapabilityWrapperManager<>(
          IEnergyWrapper.class, ForgeEnergyIntegration.class);

    public TileEntityTurbineValve() {
        super("TurbineValve");
        fluidTank = new TurbineFluidTank(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!ic2Registered && MekanismUtils.useIC2()) {
            register();
        }

        if (!world.isRemote) {
            if (structure != null) {
                double prev = getEnergy();
                CableUtils.emit(this);
            }
        }
    }

    @Override
    public boolean sideIsOutput(EnumFacing side) {
        if (structure != null) {
            return !structure.locations.contains(Coord4D.get(this).offset(side));
        }

        return false;
    }

    @Override
    public boolean sideIsConsumer(EnumFacing side) {
        return false;
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void register() {
        if (!world.isRemote) {
            IEnergyTile registered = EnergyNet.instance.getTile(world, getPos());

            if (registered != this) {
                if (registered != null) {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
                } else {
                    MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
                    ic2Registered = true;
                }
            }
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void deregister() {
        if (!world.isRemote) {
            IEnergyTile registered = EnergyNet.instance.getTile(world, getPos());

            if (registered != null) {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(registered));
            }
        }
    }

    @Override
    public double getMaxOutput() {
        return structure != null ? structure.getEnergyCapacity() : 0;
    }

    @Override
    public void onAdded() {
        super.onAdded();

        if (MekanismUtils.useIC2()) {
            register();
        }
    }

    @Override
    public void onChunkUnload() {
        if (MekanismUtils.useIC2()) {
            deregister();
        }

        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (MekanismUtils.useIC2()) {
            deregister();
        }
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        if (sideIsOutput(from)) {
            double toSend = Math.min(getEnergy(), Math.min(getMaxOutput(), maxExtract * general.FROM_RF));

            if (!simulate) {
                setEnergy(getEnergy() - toSend);
            }

            return (int) Math.round(Math.min(Integer.MAX_VALUE, toSend * general.TO_RF));
        }

        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public boolean canConnectEnergy(EnumFacing from) {
        return structure != null;
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(EnumFacing from) {
        return (int) Math.round(Math.min(Integer.MAX_VALUE, getEnergy() * general.TO_RF));
    }

    @Override
    @Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(EnumFacing from) {
        return (int) Math.round(Math.min(Integer.MAX_VALUE, getMaxEnergy() * general.TO_RF));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getSinkTier() {
        return 4;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getSourceTier() {
        return 4;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int addEnergy(int amount) {
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean isTeleporterCompatible(EnumFacing side) {
        return canOutputEnergy(side);
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return sideIsOutput(side);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
        return false;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction) {
        return sideIsOutput(direction) && receiver instanceof IEnergyConductor;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getStored() {
        return (int) Math.round(Math.min(Integer.MAX_VALUE, getEnergy() * general.TO_IC2));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void setStored(int energy) {
        setEnergy(energy * general.FROM_IC2);
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getCapacity() {
        return (int) Math.round(Math.min(Integer.MAX_VALUE, getMaxEnergy() * general.TO_IC2));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public int getOutput() {
        return (int) Math.round(Math.min(Integer.MAX_VALUE, getMaxOutput() * general.TO_IC2));
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getDemandedEnergy() {
        return 0;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getOfferedEnergy() {
        return Math.min(getEnergy(), getMaxOutput()) * general.TO_IC2;
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        return false;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double getOutputEnergyUnitsPerTick() {
        return getMaxOutput() * general.TO_IC2;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public double injectEnergy(EnumFacing direction, double amount, double voltage) {
        return amount;
    }

    @Override
    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public void drawEnergy(double amount) {
        if (structure != null) {
            double toDraw = Math.min(amount * general.FROM_IC2, getMaxOutput());
            setEnergy(Math.max(getEnergy() - toDraw, 0));
        }
    }

    @Override
    public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
        return 0;
    }

    @Override
    public double pullEnergy(EnumFacing side, double amount, boolean simulate) {
        double toGive = Math.min(getEnergy(), amount);

        if (toGive < 0.0001 || (side != null && !sideIsOutput(side))) {
            return 0;
        }

        if (!simulate) {
            setEnergy(getEnergy() - toGive);
        }

        return toGive;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) ? new FluidTankInfo[]{
              fluidTank.getInfo()} : PipeUtils.EMPTY;
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (resource == null || !canFill(from, resource)) {
            return 0;
        }

        int filled = fluidTank.fill(resource, doFill);

        if (doFill) {
            structure.newSteamInput += filled;
        }

        if (filled < structure.getFluidCapacity() && structure.dumpMode != GasMode.IDLE) {
            filled = Math.min(structure.getFluidCapacity(), resource.amount);
        }

        return filled;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        if (fluid != null && fluid.getFluid().equals(FluidRegistry.getFluid("steam"))) {
            return ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure));
        }

        return false;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize("gui.industrialTurbine");
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        if (method == 0) {
            return new Object[]{structure != null};
        } else {
            if (structure == null) {
                return new Object[]{"Unformed"};
            }

            switch (method) {
                case 1:
                    return new Object[]{structure.fluidStored != null ? structure.fluidStored.amount : 0};
                case 2:
                    return new Object[]{structure.clientFlow};
                case 3:
                    double rate =
                          structure.lowerVolume * (structure.clientDispersers * generators.turbineDisperserGasFlow);
                    rate = Math.min(rate, structure.vents * generators.turbineVentGasFlow);
                    return new Object[]{rate};
                case 4:
                    return new Object[]{structure.lastSteamInput};
            }
        }

        throw new NoSuchMethodException();
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                  || capability == Capabilities.ENERGY_STORAGE_CAPABILITY
                  || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY
                  || capability == Capabilities.TESLA_HOLDER_CAPABILITY
                  || (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && sideIsOutput(facing))
                  || capability == CapabilityEnergy.ENERGY) {
                return true;
            }
        }

        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY
                  || capability == Capabilities.ENERGY_OUTPUTTER_CAPABILITY) {
                return (T) this;
            }

            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return (T) new FluidHandlerWrapper(this, side);
            }

            if (capability == Capabilities.TESLA_HOLDER_CAPABILITY
                  || (capability == Capabilities.TESLA_PRODUCER_CAPABILITY && sideIsOutput(facing))) {
                return (T) teslaManager.getWrapper(this, facing);
            }

            if (capability == CapabilityEnergy.ENERGY) {
                return (T) forgeEnergyManager.getWrapper(this, facing);
            }
        }

        return super.getCapability(capability, side);
    }
}
