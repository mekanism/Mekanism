package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
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
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniversalCable extends BufferedTransmitter<IStrictEnergyHandler, EnergyNetwork, Long, UniversalCable> implements IMekanismStrictEnergyHandler,
      IUpgradeableTransmitter<UniversalCableUpgradeData> {

    public final CableTier tier;

    private final List<IEnergyContainer> energyContainers;
    public final BasicEnergyContainer buffer;
    public long lastWrite = 0L;

    public UniversalCable(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.ENERGY);
        this.tier = Attribute.getTier(blockProvider, CableTier.class);
        buffer = BasicEnergyContainer.create(getCapacity(), ConstantPredicates.alwaysFalse(), ConstantPredicates.alwaysTrue(), this);
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
        if (!hasPullSide || getAvailablePull() <= 0) {
            return;
        }
        EnergyAcceptorCache acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            IStrictEnergyHandler connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                long received = connectedAcceptor.extractEnergy(getAvailablePull(), Action.SIMULATE);
                if (received > 0L && takeEnergy(received, Action.SIMULATE) == 0L) {
                    //If we received some energy and are able to insert it all
                    long remainder = takeEnergy(received, Action.EXECUTE);
                    connectedAcceptor.extractEnergy(received - remainder, Action.EXECUTE);
                }
            }
        }
    }

    private long getAvailablePull() {
        if (hasTransmitterNetwork()) {
            return Math.min(getCapacity(), getTransmitterNetwork().energyContainer.getNeeded());
        }
        return Math.min(getCapacity(), buffer.getNeeded());
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
        lastWrite = nbtTags.getLong(SerializationConstants.ENERGY);
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
            nbtTags.putLong(SerializationConstants.ENERGY, lastWrite);
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
    public Long releaseShare() {
        long energy = buffer.getEnergy();
        buffer.setEmpty();
        return energy;
    }

    @NotNull
    @Override
    public Long getShare() {
        return buffer.getEnergy();
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback() == 0L;
    }

    @NotNull
    @Override
    public Long getBufferWithFallback() {
        long buffer = getShare();
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
                //Clamp the value so that we can't error if the network's energy is less than the amount we are saving
                lastWrite = Math.min(transmitterNetwork.energyContainer.getEnergy(), lastWrite);
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.getEnergy() - lastWrite);
                buffer.setEnergy(lastWrite);
            }
        }
    }

    @Override
    public long getCapacity() {
        return tier.getCableCapacity();
    }

    /**
     * @return remainder
     */
    private long takeEnergy(long amount, Action action) {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().energyContainer.insert(amount, action, AutomationType.INTERNAL);
        }
        return buffer.insert(amount, action, AutomationType.INTERNAL);
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull EnergyNetwork network, @NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleContentsUpdateTag(network, tag, provider);
        NBTUtils.setLegacyEnergyIfPresent(tag, SerializationConstants.ENERGY, network.energyContainer::setEnergy);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> network.currentScale = scale);
    }
}