package mekanism.common.lib.transmitter;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import mekanism.common.content.network.distribution.ResourceTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.BufferedResourceTransmitter;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Change the buffer to a LargeResourceStack<RESOURCE>??
public abstract class DynamicBufferedResourceNetwork<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>>
      extends DynamicBufferedNetwork<ResourceHandler<RESOURCE>, NETWORK, LargeResourceStack<RESOURCE>, TRANSMITTER> implements IContentsListener {

    protected final CONTAINER container;
    private final List<CONTAINER> containers;
    private RESOURCE lastType = getEmptyType();
    protected long prevTransferAmount;

    protected DynamicBufferedResourceNetwork(UUID networkID, ContainerCreator<RESOURCE, CONTAINER> containerCreator) {
        super(networkID);
        this.container = containerCreator.create(this::getCapacity, this);
        this.containers = Collections.singletonList(this.container);
    }

    protected abstract RESOURCE getEmptyType();

    public final CONTAINER getContainer() {
        return this.container;
    }

    public final List<CONTAINER> getContainers() {
        return this.containers;
    }

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> getBuffer() {
        //TODO - 26.1: Evaluate callers and see what can skip wrapping in a large resource stack/if we can at least cache the empty large resource stack?
        return new LargeResourceStack<>(container.getResource(), container.amount());
    }

    @Override
    public List<TRANSMITTER> adoptTransmittersAndAcceptorsFrom(NETWORK net) {
        float oldScale = currentScale;
        long oldCapacity = getCapacity();
        List<TRANSMITTER> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the chemical scales
        long capacity = getCapacity();
        currentScale = Math.min(1, capacity == 0 ? 0 : (currentScale * oldCapacity + net.currentScale * net.capacity) / capacity);
        if (isRemote()) {
            if (this.container.isEmpty()) {
                this.container.setContents(net.container.getResource(), net.container.amountAsLong());
                net.container.setEmpty();
            }
        } else {
            if (!net.container.isEmpty()) {
                if (this.container.isEmpty()) {
                    this.container.setContents(net.container.getResource(), net.container.amountAsLong());
                    net.container.setEmpty();
                } else {
                    // compare the chemicals themselves
                    if (this.container.getResource().equals(net.container.getResource())) {
                        long amount = net.container.amountAsLong();
                        //TODO - 26.1: Do we need to check for long overflow?
                        this.container.setContentsUnchecked(this.container.getResource(), this.container.amountAsLong() + amount);
                    } else {
                        Mekanism.logger.error("Incompatible buffed resource networks merged: {}, {}.", this.container.getResource(), net.container.getResource());
                    }
                    net.container.setEmpty();
                }
            }
            if (oldScale != currentScale) {
                //We want to make sure we update to the scale change
                needsUpdate = true;
            }
        }
        return transmittersToUpdate;
    }

    @Override
    protected void forceScaleUpdate() {
        if (!container.isEmpty() && getCapacity() > 0) {
            currentScale = (float) Math.min(1, container.amountAsLong() / (double) getCapacity());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public void absorbBuffer(TRANSMITTER transmitter) {
        LargeResourceStack<RESOURCE> transmitterReleased = transmitter.releaseShare();
        if (!transmitterReleased.isEmpty()) {
            if (container.isEmpty()) {
                container.setContents(transmitterReleased.resource(), transmitterReleased.amount());
            } else if (container.getResource().equals(transmitterReleased.resource())) {
                //TODO - 26.1: evaluate if we actually do want helpers for growing and shrinking for use cases like this
                container.setContentsUnchecked(transmitterReleased.resource(), container.amountAsLong() + transmitterReleased.amount());
            }
        }
    }

    @Override
    public void clampBuffer() {
        ResourceUtils.clampContents(container);
    }

    @Override
    public boolean isCompatibleWith(NETWORK other) {
        if (super.isCompatibleWith(other)) {
            return container.isEmpty() || other.container.isEmpty() || container.getResource().equals(other.container.getResource());
        }
        return false;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
        if (!container.getResource().equals(lastType)) {
            //If the type does not match update it, and mark that we need an update
            if (!container.isEmpty()) {
                lastType = container.getResource();
            }
            needsUpdate = true;
        }
    }

    public RESOURCE getLastType() {
        return lastType;
    }

    public void setLastType(@NotNull RESOURCE type) {
        if (type.isEmpty()) {
            if (!container.isEmpty()) {
                container.setEmpty();
            }
        } else {
            lastType = type;
            container.setContents(type, 1);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) (container.amountAsLong() / (double) getCapacity());
        float ret = Math.max(currentScale, scale);
        if (prevTransferAmount > 0 && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount <= 0 && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    public long getPrevTransferAmount() {
        return prevTransferAmount;
    }

    @Override
    public Component getStoredInfo() {
        if (container.isEmpty()) {
            return MekanismLang.NONE.translate();
        }
        return MekanismLang.NETWORK_MB_STORED.translate(container.getResource(), container.amountAsLong());
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    protected void onLastTransmitterRemoved(@NotNull TRANSMITTER triggerTransmitter) {
        if (!container.isEmpty()) {
            disperse(triggerTransmitter, container.getResource(), container.amountAsLong());
        }
    }

    @Override
    protected void updateSaveShares(@Nullable TRANSMITTER triggerTransmitter, TransactionContext transaction) {
        super.updateSaveShares(triggerTransmitter, transaction);
        if (!isEmpty()) {
            ResourceTransmitterSaveTarget<RESOURCE, TRANSMITTER> saveTarget = new ResourceTransmitterSaveTarget<>(getTransmitters());
            RESOURCE resource = container.getResource();
            long toSend = container.amountAsLong();
            long sent = EmitUtils.sendToAcceptors(saveTarget, toSend, resource, transaction);
            if (triggerTransmitter != null && sent < toSend) {
                disperse(triggerTransmitter, resource, toSend - sent);
            }
            saveTarget.saveShare();
        }
    }

    protected void disperse(TRANSMITTER triggerTransmitter, RESOURCE resource, long amount) {
    }

    protected void tickEmit() {
        if (container.isEmpty()) {
            prevTransferAmount = 0;
        } else {
            try (Transaction transaction = Transaction.openRoot()) {
                long current = container.amountAsLong();
                prevTransferAmount = tickEmit(container.getResource(), current, transaction);
                //TODO - 26.1: Evaluate this
                container.setContentsUnchecked(container.getResource(), current - prevTransferAmount);
                transaction.commit();
            }
        }
    }

    private long tickEmit(RESOURCE typeToSend, long amountToSend, TransactionContext transaction) {
        Collection<Map<Direction, ResourceHandler<RESOURCE>>> acceptorValues = acceptorCache.getAcceptorValues();
        ResourceHandlerTarget<RESOURCE> target = null;
        int toSendAsInt = Ints.saturatedCast(amountToSend);
        for (Map<Direction, ResourceHandler<RESOURCE>> acceptors : acceptorValues) {
            for (ResourceHandler<RESOURCE> acceptor : acceptors.values()) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (acceptor.insert(typeToSend, toSendAsInt, simulation) > 0) {
                        if (target == null) {
                            //Lazily initialize the target, which allows us to also skip attempting to start emitting
                            target = new ResourceHandlerTarget<>(acceptorValues.size() * 2);
                        }
                        target.addHandler(acceptor);
                    }
                }
            }
        }
        return EmitUtils.sendToAcceptors(target, amountToSend, typeToSend, transaction);
    }

    @FunctionalInterface
    protected interface ContainerCreator<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

        CONTAINER create(LongSupplier capacity, IContentsListener listener);
    }
}