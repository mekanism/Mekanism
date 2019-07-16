package mekanism.common.tile.transmitter;

import buildcraft.api.mj.IMjPassiveProvider;
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
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.buildcraft.MjCableIntegration;
import mekanism.common.integration.buildcraft.MjIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyCableIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
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
    private CapabilityWrapperManager mjManager = new CapabilityWrapperManager<>(getClass(), MjCableIntegration.class);

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
                        IMjPassiveProvider mjProvider;//do not assign anything to this here, or classloader issues may happen
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
                        } else if (MekanismUtils.useMj() && (mjProvider = CapabilityUtils.getCapability(outputter, Capabilities.MJ_PROVIDER_CAPABILITY, side.getOpposite())) != null) {
                            double toDraw = MjIntegration.fromMj(mjProvider.extractPower(0, MjIntegration.toMj(canDraw), true));
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            mjProvider.extractPower(0, MjIntegration.toMj(toDraw), false);
                        } else if (MekanismUtils.useForge() && (forgeStorage = CapabilityUtils.getCapability(outputter, CapabilityEnergy.ENERGY, side.getOpposite())) != null) {
                            double toDraw = ForgeEnergyIntegration.fromForge(forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(canDraw), true));
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(toDraw), false);
                        } else if (MekanismUtils.useRF() && outputter instanceof IEnergyProvider) {
                            IEnergyProvider rfProvider = (IEnergyProvider) outputter;
                            double toDraw = rfProvider.extractEnergy(side.getOpposite(), MekanismUtils.clampToInt(canDraw * MekanismConfig.current().general.TO_RF.val()), true)
                                            * MekanismConfig.current().general.FROM_RF.val();
                            if (toDraw > 0) {
                                toDraw -= takeEnergy(toDraw, true);
                            }
                            rfProvider.extractEnergy(side.getOpposite(), MekanismUtils.clampToInt(toDraw * MekanismConfig.current().general.TO_RF.val()), false);
                        } else if (MekanismUtils.useIC2()) {
                            IEnergyTile tile = EnergyNet.instance.getSubTile(outputter.getWorld(), outputter.getPos());
                            if (tile instanceof IEnergySource) {
                                double received = Math.min(((IEnergySource) tile).getOfferedEnergy() * MekanismConfig.current().general.FROM_IC2.val(), canDraw);
                                double toDraw = received;
                                if (received > 0) {
                                    toDraw -= takeEnergy(received, true);
                                }
                                ((IEnergySource) tile).drawEnergy(toDraw * MekanismConfig.current().general.TO_IC2.val());
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
        return maxReceive - MekanismUtils.clampToInt(takeEnergy(maxReceive * MekanismConfig.current().general.FROM_RF.val(), !simulate) *
                                                     MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public boolean canConnectEnergy(EnumFacing from) {
        return canConnect(from);
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getEnergyStored(EnumFacing from) {
        return MekanismUtils.clampToInt(getEnergy() * MekanismConfig.current().general.TO_RF.val());
    }

    @Override
    @Optional.Method(modid = MekanismHooks.REDSTONEFLUX_MOD_ID)
    public int getMaxEnergyStored(EnumFacing from) {
        return MekanismUtils.clampToInt(getMaxEnergy() * MekanismConfig.current().general.TO_RF.val());
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
               || capability == Capabilities.TESLA_CONSUMER_CAPABILITY || capability == CapabilityEnergy.ENERGY || capability == Capabilities.MJ_READABLE_CAPABILITY
               || capability == Capabilities.MJ_RECEIVER_CAPABILITY || capability == Capabilities.MJ_CONNECTOR_CAPABILITY
               || capability == Capabilities.MJ_PROVIDER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY || capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return (T) this;
        } else if (capability == Capabilities.TESLA_CONSUMER_CAPABILITY) {
            return (T) teslaManager.getWrapper(this, facing);
        } else if (capability == Capabilities.MJ_RECEIVER_CAPABILITY || capability == Capabilities.MJ_CONNECTOR_CAPABILITY ||
                   capability == Capabilities.MJ_PROVIDER_CAPABILITY || capability == Capabilities.MJ_READABLE_CAPABILITY) {
            return (T) mjManager.getWrapper(this, facing);
        } else if (capability == CapabilityEnergy.ENERGY) {
            return (T) forgeEnergyManager.getWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }
}