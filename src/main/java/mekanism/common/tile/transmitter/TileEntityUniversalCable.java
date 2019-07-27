package mekanism.common.tile.transmitter;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.EnergyStack;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.forgeenergy.ForgeEnergyCableIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.integration.ic2.IC2Integration;
import mekanism.common.integration.redstoneflux.RFIntegration;
import mekanism.common.integration.tesla.TeslaCableIntegration;
import mekanism.common.integration.tesla.TeslaIntegration;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.CableTier;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList(
      @Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
)
public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyAcceptorWrapper, EnergyNetwork, EnergyStack> implements IStrictEnergyAcceptor,
      IStrictEnergyStorage, IEnergyReceiver {

    public CableTier tier = CableTier.BASIC;

    public double currentPower = 0;
    public double lastWrite = 0;

    public EnergyStack buffer = new EnergyStack(0);
    private CapabilityWrapperManager teslaManager = new CapabilityWrapperManager<>(getClass(), TeslaCableIntegration.class);
    private CapabilityWrapperManager forgeEnergyManager = new CapabilityWrapperManager<>(getClass(), ForgeEnergyCableIntegration.class);

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = CableTier.get(baseTier);
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            double targetPower = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().clientEnergyScale : 0;
            if (Math.abs(currentPower - targetPower) > 0.01) {
                currentPower = (9 * currentPower + targetPower) / 10;
            }
        } else {
            updateShare();
            List<EnumFacing> sides = getConnections(ConnectionType.PULL);
            if (!sides.isEmpty()) {
                TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(this, getPos(), getWorld());
                double canDraw = tier.getCableCapacity();
                for (EnumFacing side : sides) {
                    TileEntity outputter = connectedOutputters[side.ordinal()];
                    if (outputter != null) {
                        //pre declare some variables for inline assignment & checks
                        IStrictEnergyStorage strictStorage;
                        ITeslaProducer teslaProducer;//do not assign anything to this here, or classloader issues may happen
                        IEnergyStorage forgeStorage;
                        if (CapabilityUtils.hasCapability(outputter, Capabilities.ENERGY_OUTPUTTER_CAPABILITY, side.getOpposite())
                            && (strictStorage = CapabilityUtils.getCapability(outputter, Capabilities.ENERGY_STORAGE_CAPABILITY, side.getOpposite())) != null) {
                            double received = Math.min(strictStorage.getEnergy(), canDraw);
                            double toDraw = received;
                            if (received > 0) {
                                toDraw -= takeEnergy(received, true);
                            }
                            strictStorage.setEnergy(strictStorage.getEnergy() - toDraw);
                        } else if (MekanismUtils.useTesla() && (teslaProducer = CapabilityUtils.getCapability(outputter, Capabilities.TESLA_PRODUCER_CAPABILITY, side.getOpposite())) != null) {
                            double toDraw = TeslaIntegration.fromTesla(teslaProducer.takePower(TeslaIntegration.toTesla(canDraw), true));
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            teslaProducer.takePower(TeslaIntegration.toTesla(toDraw), false);
                        } else if (MekanismUtils.useForge() && (forgeStorage = CapabilityUtils.getCapability(outputter, CapabilityEnergy.ENERGY, side.getOpposite())) != null) {
                            double toDraw = ForgeEnergyIntegration.fromForge(forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(canDraw), true));
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(toDraw), false);
                        } else if (MekanismUtils.useRF() && outputter instanceof IEnergyProvider) {
                            IEnergyProvider rfProvider = (IEnergyProvider) outputter;
                            double toDraw = RFIntegration.fromRF(rfProvider.extractEnergy(side.getOpposite(), RFIntegration.toRF(canDraw), true));
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            rfProvider.extractEnergy(side.getOpposite(), RFIntegration.toRF(toDraw), false);
                        } else if (MekanismUtils.useIC2()) {
                            IEnergyTile tile = EnergyNet.instance.getSubTile(outputter.getWorld(), outputter.getPos());
                            if (tile instanceof IEnergySource) {
                                double received = Math.min(IC2Integration.fromEU(((IEnergySource) tile).getOfferedEnergy()), canDraw);
                                double toDraw = received;
                                if (received > 0) {
                                    toDraw -= takeEnergy(received, true);
                                }
                                ((IEnergySource) tile).drawEnergy(IC2Integration.toEU(toDraw));
                            }
                        }
                    }
                }
            }
        }
        super.update();
    }

    @Override
    public void updateShare() {
        if (getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0) {
            double last = getSaveShare();
            if (last != lastWrite) {
                lastWrite = last;
                markDirty();
            }
        }
    }

    private double getSaveShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return EnergyNetwork.round(getTransmitter().getTransmitterNetwork().buffer.amount * (1F / getTransmitter().getTransmitterNetwork().transmittersSize()));
        }
        return buffer.amount;
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.UNIVERSAL_CABLE;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        buffer.amount = nbtTags.getDouble("cacheEnergy");
        if (buffer.amount < 0) {
            buffer.amount = 0;
        }
        if (nbtTags.hasKey("tier")) {
            tier = CableTier.values()[nbtTags.getInteger("tier")];
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        nbtTags.setDouble("cacheEnergy", lastWrite);
        nbtTags.setInteger("tier", tier.ordinal());
        return nbtTags;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @Override
    public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks) {
        return new EnergyNetwork(networks);
    }

    @Override
    public boolean isValidAcceptor(TileEntity acceptor, EnumFacing side) {
        return CableUtils.isValidAcceptorOnSide(MekanismUtils.getTileEntity(world, getPos()), acceptor, side);
    }

    @Override
    public EnergyNetwork createNewNetwork() {
        return new EnergyNetwork();
    }

    @Override
    public EnergyStack getBuffer() {
        return buffer;
    }

    @Override
    public void takeShare() {
        if (getTransmitter().hasTransmitterNetwork()) {
            getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite;
            buffer.amount = lastWrite;
        }
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return maxReceive - RFIntegration.toRF(takeEnergy(RFIntegration.fromRF(maxReceive), !simulate));
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public boolean canConnectEnergy(EnumFacing from) {
        return canConnect(from);
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(EnumFacing from) {
        return RFIntegration.toRF(getEnergy());
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(EnumFacing from) {
        return RFIntegration.toRF(getMaxEnergy());
    }

    @Override
    public int getCapacity() {
        return tier.getCableCapacity();
    }

    @Override
    public double acceptEnergy(EnumFacing side, double amount, boolean simulate) {
        double toUse = Math.min(getMaxEnergy() - getEnergy(), amount);
        if (toUse < 0.0001 || (side != null && !canReceiveEnergy(side))) {
            return 0;
        }
        if (!simulate) {
            setEnergy(getEnergy() + toUse);
        }
        return toUse;
    }

    @Override
    public boolean canReceiveEnergy(EnumFacing side) {
        if (side == null) {
            return true;
        }
        return getConnectionType(side) == ConnectionType.NORMAL;
    }

    @Override
    public double getMaxEnergy() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getCapacity();
        }
        return getCapacity();
    }

    @Override
    public double getEnergy() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().buffer.amount;
        }
        return buffer.amount;
    }

    @Override
    public void setEnergy(double energy) {
        if (getTransmitter().hasTransmitterNetwork()) {
            getTransmitter().getTransmitterNetwork().buffer.amount = energy;
        } else {
            buffer.amount = energy;
        }
    }

    public double takeEnergy(double energy, boolean doEmit) {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().emit(energy, doEmit);
        }
        double used = Math.min(getCapacity() - buffer.amount, energy);
        if (doEmit) {
            buffer.amount += used;
        }
        return energy - used;
    }

    @Override
    public EnergyAcceptorWrapper getCachedAcceptor(EnumFacing side) {
        return EnergyAcceptorWrapper.get(getCachedTile(side), side.getOpposite());
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = CableTier.values()[tier.ordinal() + 1];
            markDirtyTransmitters();
            sendDesc = true;
            return true;
        }
        return false;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        tier = CableTier.values()[dataStream.readInt()];
        super.handlePacketData(dataStream);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());
        super.getNetworkedData(data);
        return data;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY
               || capability == Capabilities.TESLA_CONSUMER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return (T) this;
        } else if (capability == Capabilities.TESLA_CONSUMER_CAPABILITY) {
            return (T) teslaManager.getWrapper(this, facing);
        } else if (capability == CapabilityEnergy.ENERGY) {
            return (T) forgeEnergyManager.getWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }
}