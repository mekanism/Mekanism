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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Change the buffer to a LargeResourceStack<RESOURCE>??
public abstract class BufferedResourceTransmitter<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>>
      extends BufferedTransmitter<ResourceHandler<RESOURCE>, NETWORK, LargeResourceStack<RESOURCE>, TRANSMITTER> implements IContentsListener,
      IUpgradeableTransmitter<ResourceTransmitterUpgradeData<RESOURCE>>{

    private final LargeResourceStack.StackHelper<RESOURCE> stackHelper;
    private final CONTAINER bufferContainer;
    private final List<CONTAINER> containers;

    private LargeResourceStack<RESOURCE> saveShare;

    protected BufferedResourceTransmitter(TileEntityTransmitter tile, LargeResourceStack.StackHelper<RESOURCE> stackHelper, BufferCreator<RESOURCE, CONTAINER> bufferCreator,
          TransmissionType... transmissionTypes) {
        super(tile, transmissionTypes);
        this.stackHelper = stackHelper;
        //Note: We don't allow external interactions to force pull out of our transmitters
        this.bufferContainer = bufferCreator.create(getCapacity(), ConstantPredicates.notExternal(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), this);
        this.containers = Collections.singletonList(this.bufferContainer);
        saveShare = this.stackHelper.empty();
    }

    public LargeResourceStack.StackHelper<RESOURCE> getStackHelper() {
        return this.stackHelper;
    }

    protected abstract Codec<RESOURCE> resourceCodec();;

    @Override
    public abstract IStorageTier getTier();

    @Override
    public long getCapacity() {
        return getTier().getCapacity();
    }

    public RESOURCE getCurrentSaveType() {
        return saveShare.resource();
    }

    public long getCurrentSaveAmount() {
        return saveShare.amount();
    }

    public void setSaveShare(LargeResourceStack<RESOURCE> saveShare) {
        this.saveShare = saveShare;
        getTransmitterTile().markForSave();
    }

    @Override
    public void read(@NotNull ValueInput input) {
        super.read(input);
        saveShare = stackHelper.readOrEmpty(input, SerializationConstants.STORED);
        bufferContainer.setContents(saveShare, null);
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(getTransmitter());
        }
        stackHelper.storeNonEmpty(output, SerializationConstants.STORED, saveShare);
    }

    @Override
    protected void handleContentsUpdateTag(@NotNull NETWORK network, @NotNull ValueInput input) {
        super.handleContentsUpdateTag(network, input);
        network.currentScale = input.getFloatOr(SerializationConstants.SCALE, network.currentScale);
        network.setLastType(input.read(SerializationConstants.STORED, resourceCodec()).orElse(stackHelper.empty().resource()));
    }

    @Nullable
    @Override
    public ResourceTransmitterUpgradeData<RESOURCE> getUpgradeData() {
        return new ResourceTransmitterUpgradeData<>(redstoneReactive, getConnectionTypesRaw(), bufferContainer);
    }

    @Override
    public void parseUpgradeData(@NotNull ResourceTransmitterUpgradeData<RESOURCE> data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        bufferContainer.copyContents(data.buffer);
    }

    protected CONTAINER getContainer() {
        return hasTransmitterNetwork() ? getTransmitterNetwork().getContainer() : bufferContainer;
    }

    @NotNull
    public List<CONTAINER> getContainers() {
        if (hasTransmitterNetwork()) {
            return getTransmitterNetwork().getContainers();
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

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> getShare() {
        return bufferContainer.asStack();
    }

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> releaseShare() {
        LargeResourceStack<RESOURCE> share = getShare();
        bufferContainer.setEmpty();
        return share;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            CONTAINER networkContainer = getTransmitterNetwork().getContainer();
            if (!networkContainer.isEmpty() && !saveShare.isEmpty()) {
                //TODO - 26.1: Re-evaluate this:
                // I got a crash when force closing the game: Expected value to be non-negative: -8000
                // Why is there a case that this can be negative? We could clamp it but it might be indicitive of a bug
                networkContainer.setContents(networkContainer.resource(), networkContainer.amountAsLong() - getCurrentSaveAmount(), null);
                //TODO - 26.1: Should we have a transaction context for taking shares?
                bufferContainer.setContents(saveShare, null);
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

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> getBufferWithFallback() {
        LargeResourceStack<RESOURCE> buffer = getShare();
        //If we don't have a buffer try falling back to the network's buffer
        if (buffer.isEmpty() && hasTransmitterNetwork()) {
            return getTransmitterNetwork().getBuffer();
        }
        return buffer;
    }

    protected RESOURCE getBufferOrFallback() {
        RESOURCE buffer = getBufferWithFallback().resource();
        if (buffer.isEmpty() && hasTransmitterNetwork() && getTransmitterNetwork().getPrevTransferAmount() > 0) {
            return getTransmitterNetwork().getLastType();
        }
        return buffer;
    }

    private int getAvailablePull() {
        CONTAINER container = getContainer();
        return Math.min(getTier().getTransferRate(), container.getNeededAsInt(container.resource()));
    }

    @Override
    public void pullFromAcceptors() {
        if (!hasPullSide || getAvailablePull() <= 0) {
            return;
        }
        AcceptorCache<ResourceHandler<RESOURCE>> acceptorCache = getAcceptorCache();
        for (Direction side : EnumUtils.DIRECTIONS) {
            if (!isConnectionType(side, ConnectionType.PULL)) {
                continue;
            }
            ResourceHandler<RESOURCE> connectedAcceptor = acceptorCache.getConnectedAcceptor(side);
            if (connectedAcceptor != null) {
                //Note: We recheck the buffer each time in case we ended up accepting the resource somewhere
                // and our buffer changed and is no longer empty
                RESOURCE receivedType = ResourceUtils.getTypeToExtract(getBufferWithFallback().resource(), connectedAcceptor, ConstantPredicates.alwaysTrue(), null);
                if (receivedType.isEmpty()) {
                    return;
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    int extracted = connectedAcceptor.extract(receivedType, getAvailablePull(), transaction);
                    int inserted = getContainer().insert(receivedType, extracted, transaction, AutomationType.INTERNAL);
                    if (inserted == extracted) {
                        //If we received some resource and are able to insert it all, then actually extract it and insert it into our thing.
                        // Note: We extract first after simulating ourselves because if the target gave a faulty simulation value, we want to handle it properly
                        // and not accidentally dupe anything, and we know our simulation we just performed on taking it is valid
                        transaction.commit();
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
}