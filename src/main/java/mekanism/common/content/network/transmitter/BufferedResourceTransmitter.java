package mekanism.common.content.network.transmitter;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.DynamicBufferedResourceNetwork;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

//TODO - 26.1: Change the buffer to a LargeResourceStack<RESOURCE>??
public abstract class BufferedResourceTransmitter<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>>
      extends BufferedTransmitter<ResourceHandler<RESOURCE>, NETWORK, LargeResourceStack<RESOURCE>, TRANSMITTER> implements IContentsListener {

    private final LargeResourceStack<RESOURCE> emptyStack = new LargeResourceStack<>(getEmptyResource(), 0);
    private final Codec<LargeResourceStack<RESOURCE>> resourceStackCodec;
    private final CONTAINER bufferContainer;
    private final List<CONTAINER> containers;

    private LargeResourceStack<RESOURCE> saveShare;

    protected BufferedResourceTransmitter(TileEntityTransmitter tile, Codec<LargeResourceStack<RESOURCE>> resourceStackCodec, BufferCreator<RESOURCE, CONTAINER> bufferCreator,
          TransmissionType... transmissionTypes) {
        super(tile, transmissionTypes);
        this.resourceStackCodec = resourceStackCodec;
        this.bufferContainer = bufferCreator.create(getCapacity(), this);
        this.containers = Collections.singletonList(this.bufferContainer);
        saveShare = emptyStack;
    }

    public abstract RESOURCE getEmptyResource();

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<ResourceHandler<RESOURCE>> getAcceptorCache() {
        return (AcceptorCache<ResourceHandler<RESOURCE>>) super.getAcceptorCache();
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
        saveShare = input.read(SerializationConstants.STORED, resourceStackCodec).orElse(emptyStack);
        bufferContainer.setContents(getCurrentSaveType(),  getCurrentSaveAmount());
    }

    @Override
    public void write(@NotNull ValueOutput output) {
        super.write(output);
        if (hasTransmitterNetwork()) {
            getTransmitterNetwork().validateSaveShares(getTransmitter());
        }
        //TODO - 26.1: Validate if stored is fine to use as the key, or if that conflicts with another key we might have
        if (saveShare.isEmpty()) {
            output.discard(SerializationConstants.STORED);
        } else {
            output.store(SerializationConstants.STORED, resourceStackCodec, saveShare);
        }
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
        if (bufferContainer.isEmpty()) {
            return emptyStack;
        }
        return new LargeResourceStack<>(bufferContainer.getResource(), bufferContainer.amountAsLong());
    }

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> releaseShare() {
        LargeResourceStack<RESOURCE> share = getShare();
        //TODO - 26.1: Chemical tanks used to skip setting empty if it was already empty, do we care
        bufferContainer.setEmpty();
        return share;
    }

    @Override
    public void takeShare() {
        if (hasTransmitterNetwork()) {
            CONTAINER networkContainer = getTransmitterNetwork().getContainer();
            if (!networkContainer.isEmpty() && !saveShare.isEmpty()) {
                RESOURCE currentSaveType = getCurrentSaveType();
                long amount = getCurrentSaveAmount();
                //TODO - 26.1: Re-evaluate this
                networkContainer.setContentsUnchecked(networkContainer.getResource(), networkContainer.amountAsLong() - amount);
                bufferContainer.setContents(currentSaveType, amount);
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

    protected abstract int getAvailablePull();

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
                LargeResourceStack<RESOURCE> bufferWithFallback = getBufferWithFallback();
                pullFromAcceptor(connectedAcceptor, bufferWithFallback.resource(), bufferWithFallback.isEmpty());
            }
        }
    }

    /**
     * @param connectedAcceptor  The acceptor
     * @param bufferWithFallback The buffer of the network
     * @param bufferIsEmpty      {@code true} if the buffer is empty, {@code false} otherwise
     */
    private void pullFromAcceptor(ResourceHandler<RESOURCE> connectedAcceptor, RESOURCE bufferWithFallback, boolean bufferIsEmpty) {
        if (connectedAcceptor == null) {
            return;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            RESOURCE receivedType;
            if (bufferIsEmpty) {
                //If we don't have a resource stored try pulling as much as we are able to
                receivedType = ResourceHandlerUtil.findExtractableResource(connectedAcceptor, ConstantPredicates.alwaysTrue(), transaction);
            } else {
                //Otherwise, try draining the same type of resource we have stored requesting up to as much as we are able to pull
                // We do this to better support multiple tanks in case the resource we have stored we could pull out of a block's
                // second tank but just asking to drain a specific amount
                receivedType = bufferWithFallback;
            }
            if (receivedType == null || receivedType.isEmpty()) {
                return;
            }
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

    @FunctionalInterface
    protected interface BufferCreator<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

        CONTAINER create(long capacity, IContentsListener listener);
    }
}