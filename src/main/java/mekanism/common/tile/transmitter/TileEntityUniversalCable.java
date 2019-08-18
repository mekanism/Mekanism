package mekanism.common.tile.transmitter;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.api.energy.EnergyStack;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.IBlockProvider;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.CapabilityWrapperManager;
import mekanism.common.integration.forgeenergy.ForgeEnergyCableIntegration;
import mekanism.common.integration.forgeenergy.ForgeEnergyIntegration;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.CableTier;
import mekanism.common.transmitters.grid.EnergyNetwork;
import mekanism.common.util.CableUtils;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileEntityUniversalCable extends TileEntityTransmitter<EnergyAcceptorWrapper, EnergyNetwork, EnergyStack> implements IStrictEnergyAcceptor,
      IStrictEnergyStorage {

    public CableTier tier;

    public double currentPower = 0;
    public double lastWrite = 0;

    public EnergyStack buffer = new EnergyStack(0);
    private CapabilityWrapperManager<TileEntityUniversalCable, ForgeEnergyCableIntegration> forgeEnergyManager =
          new CapabilityWrapperManager<>(TileEntityUniversalCable.class, ForgeEnergyCableIntegration.class);

    public TileEntityUniversalCable(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityUniversalCable>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockUniversalCable) blockProvider.getBlock()).getTier();
    }

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = CableTier.get(baseTier);
    }

    @Override
    public void tick() {
        if (getWorld().isRemote) {
            double targetPower = getTransmitter().hasTransmitterNetwork() ? getTransmitter().getTransmitterNetwork().clientEnergyScale : 0;
            if (Math.abs(currentPower - targetPower) > 0.01) {
                currentPower = (9 * currentPower + targetPower) / 10;
            }
        } else {
            updateShare();
            List<Direction> sides = getConnections(ConnectionType.PULL);
            if (!sides.isEmpty()) {
                TileEntity[] connectedOutputters = CableUtils.getConnectedOutputters(this, getPos(), getWorld());
                double maxDraw = tier.getCableCapacity();
                for (Direction side : sides) {
                    TileEntity outputter = connectedOutputters[side.ordinal()];
                    if (outputter != null) {
                        CapabilityUtils.getCapabilityHelper(outputter, Capabilities.ENERGY_STORAGE_CAPABILITY, side.getOpposite()).ifPresentElse(
                              //Strict Energy
                              strictStorage -> {
                                  double received = draw(Math.min(strictStorage.getEnergy(), maxDraw));
                                  strictStorage.setEnergy(strictStorage.getEnergy() - received);
                              },
                              //Else
                              //TODO: IC2
                              //ifPresentElse
                              () -> CapabilityUtils.getCapabilityHelper(outputter, CapabilityEnergy.ENERGY, side.getOpposite()).ifPresent(
                                    //Forge Energy
                                    forgeStorage -> {
                                        double received = draw(ForgeEnergyIntegration.fromForge(forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(maxDraw), true)));
                                        forgeStorage.extractEnergy(ForgeEnergyIntegration.toForge(received), false);
                                    }
                                    //TODO: IC2
                                    /*,
                                    //Else IC2
                                    () -> {
                                        if (MekanismUtils.useIC2()) {
                                            IEnergyTile tile = EnergyNet.instance.getSubTile(outputter.getWorld(), outputter.getPos());
                                            if (tile instanceof IEnergySource) {
                                                double received = draw(Math.min(IC2Integration.fromEU(((IEnergySource) tile).getOfferedEnergy()), maxDraw));
                                                ((IEnergySource) tile).drawEnergy(IC2Integration.toEU(received));
                                            }
                                        }
                                    }*/
                              )
                        );
                    }
                }
            }
        }
        super.tick();
    }

    /**
     * Takes a certain amount of energy and returns how much was actually taken
     *
     * @param toDraw Amount to take
     *
     * @return Amount actually taken
     */
    private double draw(double toDraw) {
        if (toDraw > 0) {
            toDraw -= takeEnergy(toDraw, true);
        }
        return toDraw;
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        buffer.amount = nbtTags.getDouble("cacheEnergy");
        if (buffer.amount < 0) {
            buffer.amount = 0;
        }
        if (nbtTags.contains("tier")) {
            tier = CableTier.values()[nbtTags.getInt("tier")];
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("cacheEnergy", lastWrite);
        nbtTags.putInt("tier", tier.ordinal());
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
    public boolean isValidAcceptor(TileEntity acceptor, Direction side) {
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
    public int getCapacity() {
        return tier.getCableCapacity();
    }

    @Override
    public double acceptEnergy(Direction side, double amount, boolean simulate) {
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
    public boolean canReceiveEnergy(Direction side) {
        if (side == null) {
            return true;
        }
        return getConnectionType(side) == ConnectionType.NORMAL;
    }

    @Override
    public double getMaxEnergy() {
        if (getTransmitter().hasTransmitterNetwork()) {
            return getTransmitter().getTransmitterNetwork().getCapacityAsDouble();
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

    /**
     * @return Amount of left over energy
     */
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
    public EnergyAcceptorWrapper getCachedAcceptor(Direction side) {
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
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        tier = CableTier.values()[dataStream.readInt()];
        super.handlePacketData(dataStream);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());
        super.getNetworkedData(data);
        return data;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY) {
            return Capabilities.ENERGY_STORAGE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.ENERGY_ACCEPTOR_CAPABILITY) {
            return Capabilities.ENERGY_ACCEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.orEmpty(capability, LazyOptional.of(() -> forgeEnergyManager.getWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }
}