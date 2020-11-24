package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.EnergyAcceptorCache;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.upgrade.transmitter.UniversalCableUpgradeData;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class UniversalCable extends BufferedTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong, UniversalCable> implements IMekanismStrictEnergyHandler,
      IUpgradeableTransmitter<UniversalCableUpgradeData> {

    public final CableTier tier;

    private final List<IEnergyContainer> energyContainers;
    public final BasicEnergyContainer buffer;
    public FloatingLong lastWrite = FloatingLong.ZERO;

    public UniversalCable(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.ENERGY);
        this.tier = Attribute.getTier(blockProvider.getBlock(), CableTier.class);
        buffer = BasicEnergyContainer.create(getCapacityAsFloatingLong(), BasicEnergyContainer.alwaysFalse, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(buffer);
    }

    @Override
    protected EnergyAcceptorCache createAcceptorCache() {
        return new EnergyAcceptorCache(this, getTransmitterTile());
    }

    @Override
    public EnergyAcceptorCache getAcceptorCache() {
        return (EnergyAcceptorCache) super.getAcceptorCache();
    }

    @Override
    public CableTier getTier() {
        return tier;
    }

    @Override
    public void pullFromAcceptors() {
        Set<Direction> connections = getConnections(ConnectionType.PULL);
        if (!connections.isEmpty()) {
            for (IStrictEnergyHandler connectedAcceptor : getAcceptorCache().getConnectedAcceptors(connections)) {
                FloatingLong received = connectedAcceptor.extractEnergy(getAvailablePull(), Action.SIMULATE);
                if (!received.isZero() && takeEnergy(received, Action.SIMULATE).isZero()) {
                    //If we received some energy and are able to insert it all
                    FloatingLong remainder = takeEnergy(received, Action.EXECUTE);
                    connectedAcceptor.extractEnergy(received.subtract(remainder), Action.EXECUTE);
                }
            }
        }
    }

    private FloatingLong getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return getCapacityAsFloatingLong().min(getTransmitterNetwork().energyContainer.getNeeded());
        }
        return getCapacityAsFloatingLong().min(buffer.getNeeded());
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getEnergyContainers(side);
        }
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().markDirty(false);
    }

    @Nullable
    @Override
    public UniversalCableUpgradeData getUpgradeData() {
        return new UniversalCableUpgradeData(redstoneReactive, getConnectionTypesRaw(), buffer);
    }

    @Override
    public boolean dataTypeMatches(@Nonnull TransmitterUpgradeData data) {
        return data instanceof UniversalCableUpgradeData;
    }

    @Override
    public void parseUpgradeData(@Nonnull UniversalCableUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        buffer.setEnergy(data.buffer.getEnergy());
    }

    @Override
    public void read(@Nonnull CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains(NBTConstants.ENERGY_STORED, NBT.TAG_STRING)) {
            try {
                lastWrite = FloatingLong.parseFloatingLong(nbtTags.getString(NBTConstants.ENERGY_STORED));
            } catch (NumberFormatException e) {
                lastWrite = FloatingLong.ZERO;
            }
        } else {
            lastWrite = FloatingLong.ZERO;
        }
        buffer.setEnergy(lastWrite);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (lastWrite.isZero()) {
            nbtTags.remove(NBTConstants.ENERGY_STORED);
        } else {
            nbtTags.putString(NBTConstants.ENERGY_STORED, lastWrite.toString());
        }
        return nbtTags;
    }

    @Override
    public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks) {
        return new EnergyNetwork(networks);
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return super.isValidAcceptor(tile, side) && getAcceptorCache().hasStrictEnergyHandlerAndListen(tile, side);
    }

    @Override
    public EnergyNetwork createEmptyNetwork() {
        return new EnergyNetwork();
    }

    @Override
    public EnergyNetwork createEmptyNetworkWithID(UUID networkID) {
        return new EnergyNetwork(networkID);
    }

    @Nonnull
    @Override
    public FloatingLong releaseShare() {
        FloatingLong energy = buffer.getEnergy();
        buffer.setEmpty();
        return energy;
    }

    @Nonnull
    @Override
    public FloatingLong getShare() {
        return buffer.getEnergy();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isZero();
    }

    @Nonnull
    @Override
    public FloatingLong getBufferWithFallback() {
        FloatingLong buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isZero() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            EnergyNetwork transmitterNetwork = getTransmitterNetwork();
            if (!transmitterNetwork.energyContainer.isEmpty() && !lastWrite.isZero()) {
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy().subtract(lastWrite));
                buffer.setEnergy(lastWrite);
            }
        }
    }

    @Nonnull
    public FloatingLong getCapacityAsFloatingLong() {
        return tier.getCableCapacity();
    }

    @Override
    public long getCapacity() {
        return getCapacityAsFloatingLong().longValue();
    }

    /**
     * @return remainder
     */
    private FloatingLong takeEnergy(FloatingLong amount, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        return buffer.insert(amount, action, AutomationType.INTERNAL);
    }

    @Override
    protected void handleContentsUpdateTag(@Nonnull EnergyNetwork network, @Nonnull CompoundNBT tag) {
        super.handleContentsUpdateTag(network, tag);
        NBTUtils.setFloatingLongIfPresent(tag, NBTConstants.ENERGY_STORED, network.energyContainer::setEnergy);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> network.currentScale = scale);
    }
}