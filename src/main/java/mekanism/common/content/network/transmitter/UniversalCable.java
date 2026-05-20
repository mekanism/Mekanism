package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.energy.IEnergyContainer;
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
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniversalCable extends BufferedTransmitter<IStrictEnergyHandler, EnergyNetwork, Long, UniversalCable> implements IContentsListener,
      IUpgradeableTransmitter<UniversalCableUpgradeData> {

    public final CableTier tier;

    private final List<IEnergyContainer> energyContainers;
    public final BasicEnergyContainer buffer;
    public long lastWrite = 0L;

    public UniversalCable(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        this.tier = Attribute.getTier(blockProvider, CableTier.class);
        super(tile, TransmissionType.ENERGY);
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
        IEnergyContainer buffer = getContainer();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            IStrictEnergyHandler connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    long extracted = connectedAcceptor.extract(getAvailablePull(), transaction);
                    long inserted = buffer.insert(extracted, transaction, AutomationType.INTERNAL);
                    if (inserted == extracted) {
                        //If we received some resource and are able to insert it all, then actually extract it and insert it into our thing.
                        // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
                        // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
                        transaction.commit();
                        //TODO - 26.1: Break if the buffer becomes full?
                    }
                }
            }
        }
    }

    private long getAvailablePull() {
        return Math.min(getCapacity(), getContainer().getNeeded());
    }

    protected IEnergyContainer getContainer() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().energyContainer : buffer;
    }

    @NotNull
    public List<IEnergyContainer> getEnergyContainers() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getEnergyContainers();
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
        buffer.copyContents(data.buffer);
    }

    @Override
    public void read(@NotNull ValueInput input) {
        super.read(input);
        lastWrite = input.getLongOr(SerializationConstants.ENERGY, lastWrite);
        buffer.setEnergy(lastWrite, null);
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(this);
        }
        if (lastWrite == 0L) {
            output.discard(SerializationConstants.ENERGY);
        } else {
            output.putLong(SerializationConstants.ENERGY, lastWrite);
        }
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
        long energy = buffer.energy();
        buffer.setEnergy(0, null);
        return energy;
    }

    @NotNull
    @Override
    public Long getShare() {
        return buffer.energy();
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
                lastWrite = Math.min(transmitterNetwork.energyContainer.energy(), lastWrite);
                transmitterNetwork.energyContainer.setEnergy(transmitterNetwork.energyContainer.energy() - lastWrite, null);
                buffer.setEnergy(lastWrite, null);
            }
        }
    }

    @Override
    public long getCapacity() {
        return tier.getCableCapacity();
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull EnergyNetwork network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        input.getLong(SerializationConstants.ENERGY).ifPresent(energy -> network.energyContainer.setEnergy(energy, null));
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
    }
}