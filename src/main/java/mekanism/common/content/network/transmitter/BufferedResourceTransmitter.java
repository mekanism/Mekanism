package mekanism.common.content.network.transmitter;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.IStorageTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ResourceTransmitterUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class BufferedResourceTransmitter<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>>
      extends BufferedTransmitter<ResourceHandler<RESOURCE>, NETWORK, LargeResourceStack<RESOURCE>, TRANSMITTER> implements IContentsListener,
      IUpgradeableTransmitter<ResourceTransmitterUpgradeData<RESOURCE>>{

    private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;
    private final SaveShareJournal saveShareJournal;
    private final CONTAINER bufferContainer;
    private final List<CONTAINER> containers;

    protected BufferedResourceTransmitter(TileEntityTransmitter tile, BufferCreator<RESOURCE, CONTAINER> bufferCreator, TransmissionType... transmissionTypes) {
        super(tile, transmissionTypes);
        //Note: We don't allow external interactions to force pull out of our transmitters
        this.bufferContainer = bufferCreator.create(getCapacity(), ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), this);
        this.containers = Collections.singletonList(this.bufferContainer);
        this.stackHelper = this.bufferContainer.stackHelper();
        this.saveShareJournal = new SaveShareJournal();
    }

    protected abstract Codec<RESOURCE> resourceCodec();

    @Override
    public abstract IStorageTier getTier();

    @Override
    public long getCapacity() {
        return getTier().getCapacity();
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        saveShareJournal.saveShare = stackHelper.readOrEmpty(input, SerializationConstants.STORED);
        bufferContainer.setContents(saveShareJournal.saveShare, null);
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetworkNN().validateSaveShares(getTransmitter(), null);
        }
        stackHelper.storeNonEmpty(output, SerializationConstants.STORED, saveShareJournal.saveShare);
    }

    @Override
    protected void handleContentsUpdateTag(NETWORK network, ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
        network.setLastType(input.read(SerializationConstants.STORED, resourceCodec()).orElse(stackHelper.empty().resource()));
    }

    @Override
    public ResourceTransmitterUpgradeData<RESOURCE> getUpgradeData() {
        return new ResourceTransmitterUpgradeData<>(redstoneReactive, getConnectionTypesRaw(), bufferContainer);
    }

    @Override
    public void parseUpgradeData(ResourceTransmitterUpgradeData<RESOURCE> data, TransactionContext transaction) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        bufferContainer.copyContents(data.buffer, transaction);
    }

    protected CONTAINER getContainer() {
        return hasTransmitterNetwork() ? getTransmitterNetworkNN().getContainer() : bufferContainer;
    }

    public List<CONTAINER> getContainers() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetworkNN().getContainers();
        }
        return containers;
    }

    @Override
    protected boolean canHaveIncompatibleNetworks() {
        return true;
    }

    @Override
    public void onContentsChanged() {
        getTransmitterTile().setChanged();
    }

    public SaveShareJournal startNewSaveShare(TransactionContext transaction) {
        saveShareJournal.markForNewSave(transaction);
        return saveShareJournal;
    }

    @Override
    public LargeResourceStack<RESOURCE> getShare() {
        return bufferContainer.asStack();
    }

    @Override
    public LargeResourceStack<RESOURCE> releaseShare() {
        LargeResourceStack<RESOURCE> share = getShare();
        bufferContainer.setContents(stackHelper.empty(), null);
        return share;
    }

    @Override
    public void takeShare(@Nullable TransactionContext transaction) {
        if (hasTransmitterNetwork()) {
            CONTAINER networkContainer = getTransmitterNetworkNN().getContainer();
            if (!networkContainer.isEmpty() && !saveShareJournal.saveShare.isEmpty()) {
                networkContainer.setContents(networkContainer.resource(), networkContainer.amountAsLong() - saveShareJournal.saveShare.amount(), transaction);
                bufferContainer.setContents(saveShareJournal.saveShare, transaction);
            }
        }
    }

    protected boolean isValidTransmitter(TRANSMITTER other) {
        RESOURCE buffer = getBufferOrFallback();
        if (buffer.isEmpty()) {
            return true;
        }
        RESOURCE otherBuffer = other.getBufferOrFallback();
        return otherBuffer.isEmpty() || buffer.equals(otherBuffer);
    }

    @Override
    public boolean noBufferOrFallback() {
        return getBufferWithFallback().isEmpty();
    }

    @Override
    public LargeResourceStack<RESOURCE> getBufferWithFallback() {
        LargeResourceStack<RESOURCE> buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetworkNN().getBuffer();
        }
        return buffer;
    }

    protected RESOURCE getBufferOrFallback() {
        RESOURCE buffer = getBufferWithFallback().resource();
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            NETWORK network = getTransmitterNetworkNN();
            if (network.getPrevTransferAmount() > 0) {
                return network.getLastType();
            }
        }
        return buffer;
    }

    private int getAvailablePull() {
        CONTAINER container = getContainer();
        return Math.min(getTier().getTransferRate(), container.getNeededAsInt(container.resource()));
    }

    @Override
    public void pullFromAcceptors(ServerLevel level) {
        if (!hasPullSide || getAvailablePull() <= 0) {
            return;
        }
        CONTAINER container = getContainer();
        AcceptorCache<ResourceHandler<RESOURCE>> acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            ResourceHandler<RESOURCE> connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                RESOURCE receivedType = ResourceUtils.getTypeToExtract(container.resource(), connectedAcceptor, ConstantPredicates.alwaysTrue(), null);
                if (!receivedType.isEmpty()) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        int extracted = connectedAcceptor.extract(receivedType, getAvailablePull(), transaction);
                        if (extracted > 0 && container.insert(receivedType, extracted, transaction, AutomationType.INTERNAL) == extracted) {
                            //If we received some resource and are able to insert it all, then actually extract it and insert it into our thing.
                            // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
                            // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
                            transaction.commit();
                            if (container.isFull()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @FunctionalInterface
    protected interface BufferCreator<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

        CONTAINER create(long capacity, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert, Predicate<RESOURCE> validator,
              IContentsListener listener);
    }

    public class SaveShareJournal extends SnapshotJournal<LargeResourceStack<RESOURCE>> {

        private LargeResourceStack<RESOURCE> saveShare = stackHelper.empty();

        private void markForNewSave(TransactionContext transaction) {
            updateSnapshots(transaction);
            saveShare = stackHelper.empty();
        }

        public Long accept(RESOURCE type, long amount, TransactionContext transaction) {
            if (amount == 0 || !saveShare.isEmpty() && !saveShare.matches(type)) {
                //If there is nothing being accepted (I don't think this ever happens, but validate it)
                // or if the type doesn't match, fail
                return 0L;
            }
            long toAccept = Math.min(amount, getCapacity() - saveShare.amount());
            if (toAccept > 0) {
                updateSnapshots(transaction);
                saveShare = stackHelper.createStack(type, saveShare.amount() + toAccept);
            }
            return toAccept;
        }

        @Override
        protected LargeResourceStack<RESOURCE> createSnapshot() {
            return saveShare;
        }

        @Override
        protected void revertToSnapshot(LargeResourceStack<RESOURCE> snapshot) {
            this.saveShare = snapshot;
        }

        @Override
        protected final void onRootCommit(LargeResourceStack<RESOURCE> originalState) {
            super.onRootCommit(originalState);
            if (!this.saveShare.equals(originalState)) {
                getTransmitterTile().markForSave();
            }
        }
    }
}