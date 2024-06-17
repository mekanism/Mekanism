package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
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
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniversalCable extends BufferedTransmitter<IStrictEnergyHandler, EnergyNetwork, @Unsigned Long, UniversalCable> implements IMekanismStrictEnergyHandler,
      IUpgradeableTransmitter<UniversalCableUpgradeData> {

    public final CableTier tier;

    private final List<IEnergyContainer> energyContainers;
    public final BasicEnergyContainer buffer;
    public @Unsigned long lastWrite = 0L;

    public UniversalCable(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.ENERGY);
        this.tier = Attribute.getTier(blockProvider, CableTier.class);
        buffer = BasicEnergyContainer.create(getCapacityAsUnsignedLong(), BasicEnergyContainer.alwaysFalse, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(buffer);
    }

    @Override
    protected EnergyAcceptorCache createAcceptorCache() {
        return new EnergyAcceptorCache(getTransmitterTile());
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
                @Unsigned long received = connectedAcceptor.extractEnergy(getAvailablePull(), Action.SIMULATE);
                if (received != 0L && takeEnergy(received, Action.SIMULATE) == 0L) {
                    //If we received some energy and are able to insert it all
                    @Unsigned long remainder = takeEnergy(received, Action.EXECUTE);
                    connectedAcceptor.extractEnergy(received - remainder, Action.EXECUTE);
                }
            }
        }
    }

    private @Unsigned long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return ULong.min(getCapacityAsUnsignedLong(), getTransmitterNetwork().energyContainer.getNeeded());
        }
        return ULong.min(getCapacityAsUnsignedLong(), buffer.getNeeded());
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getEnergyContainers(side);
        }
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().setChanged();
    }

    @Nullable
    @Override
    public UniversalCableUpgradeData getUpgradeData() {
        return new UniversalCableUpgradeData(redstoneReactive, getConnectionTypesRaw(), buffer);
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof UniversalCableUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull UniversalCableUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        buffer.setEnergy(data.buffer.getEnergy());
    }

    @Override
    public void read(HolderLookup.Provider provider, @NotNull CompoundTag nbtTags) {
        super.read(provider, nbtTags);
        if (nbtTags.contains(SerializationConstants.ENERGY, Tag.TAG_STRING)) {
            try {
                lastWrite = Long.parseUnsignedLong(nbtTags.getString(SerializationConstants.ENERGY));
            } catch (NumberFormatException e) {
                lastWrite = 0L;
            }
        } else {
            lastWrite = 0L;
        }
        buffer.setEnergy(lastWrite);
    }

    @NotNull
    @Override
    public CompoundTag write(HolderLookup.Provider provider, @NotNull CompoundTag nbtTags) {
        super.write(provider, nbtTags);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (lastWrite == 0L) {
            nbtTags.remove(SerializationConstants.ENERGY);
        } else {
            nbtTags.putString(SerializationConstants.ENERGY, Long.toUnsignedString(lastWrite));
        }
        return nbtTags;
    }

    @Override
    public EnergyNetwork createNetworkByMerging(Collection<EnergyNetwork> networks) {
        return new EnergyNetwork(networks);
    }

    @Override
    public EnergyNetwork createEmptyNetworkWithID(UUID networkID) {
        return new EnergyNetwork(networkID);
    }

    @NotNull
    @Override
    public @Unsigned Long releaseShare() {
        @Unsigned long energy = buffer.getEnergy();
        buffer.setEmpty();
        return energy;
    }

    @NotNull
    @Override
    public @Unsigned Long getShare() {
        return buffer.getEnergy();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback() == 0L;
    }

    @NotNull
    @Override
    public @Unsigned Long getBufferWithFallback() {
        @Unsigned long buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer == 0L && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            EnergyNetwork transmitterNetwork = getTransmitterNetwork();
            if (!transmitterNetwork.energyContainer.isEmpty() && lastWrite != 0L) {
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy() - lastWrite);
                buffer.setEnergy(lastWrite);
            }
        }
    }

    public @Unsigned long getCapacityAsUnsignedLong() {
        return tier.getCableCapacity();
    }

    @Override
    public long getCapacity() {
        return ULong.clampToSigned(getCapacityAsUnsignedLong());
    }

    /**
     * @return remainder
     */
    private @Unsigned long takeEnergy(@Unsigned long amount, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        return buffer.insert(amount, action, AutomationType.INTERNAL);
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull EnergyNetwork network, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleContentsUpdateTag(network, tag, provider);
        //TODO 1.22 - backcompat
        NBTUtils.setFloatingLongIfPresent(tag, SerializationConstants.ENERGY, energy -> network.energyContainer.setEnergy(energy.getValue()));
        NBTUtils.setLongIfPresent(tag, SerializationConstants.ENERGY, network.energyContainer::setEnergy);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> network.currentScale = scale);
    }
}