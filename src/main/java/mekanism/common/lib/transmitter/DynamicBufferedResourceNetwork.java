package mekanism.common.lib.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.content.network.distribution.ResourceTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.BufferedResourceTransmitter;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBufferedResourceNetwork<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>,
      NETWORK extends DynamicBufferedResourceNetwork<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>,
      TRANSMITTER extends BufferedResourceTransmitter<RESOURCE, CONTAINER, NETWORK, TRANSMITTER>>
      extends DynamicBufferedNetwork<ResourceHandler<RESOURCE>, NETWORK, LargeResourceStack<RESOURCE>, TRANSMITTER> {

    protected final CONTAINER container;
    private final List<CONTAINER> containers;
    private RESOURCE lastType = containerType().emptyResource();
    private long prevTransferAmount;

    protected DynamicBufferedResourceNetwork(UUID networkID, ContainerCreator<RESOURCE, CONTAINER> containerCreator) {
        super(networkID);
        this.container = containerCreator.create(this::getCapacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), this);
        this.containers = Collections.singletonList(this.container);
    }

    protected abstract ResourceContainerType<RESOURCE, CONTAINER> containerType();

    public final CONTAINER getContainer() {
        return this.container;
    }

    public final List<CONTAINER> getContainers() {
        return this.containers;
    }

    @NotNull
    @Override
    public LargeResourceStack<RESOURCE> getBuffer() {
        return container.asStack();
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
                this.container.copyContents(net.container);
                net.container.setContents(net.container.stackHelper().empty(), null);
            }
        } else {
            if (!net.container.isEmpty()) {
                if (this.container.isEmpty()) {
                    this.container.copyContents(net.container);
                    net.container.setContents(net.container.stackHelper().empty(), null);
                } else {
                    // compare the chemicals themselves
                    if (this.container.resource().equals(net.container.resource())) {
                        long amount = net.container.amountAsLong();
                        this.container.setContents(this.container.resource(), MathUtils.addClamped(this.container.amountAsLong(), amount), null);
                    } else {
                        Mekanism.logger.error("Incompatible buffed resource networks merged: {}, {}.", this.container.resource(), net.container.resource());
                    }
                    net.container.setContents(net.container.stackHelper().empty(), null);
                }
            }
            if (!Mth.equal(oldScale, currentScale)) {
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
    public void absorbBuffer(TRANSMITTER transmitter, TransactionContext transaction) {
        LargeResourceStack<RESOURCE> transmitterReleased = transmitter.releaseShare();
        if (!transmitterReleased.isEmpty()) {
            if (container.isEmpty() || transmitterReleased.matches(container.resource())) {
                container.setContents(transmitterReleased.resource(), container.amountAsLong() + transmitterReleased.amount(), transaction);
            }
        }
    }

    @Override
    public void clampBuffer() {
        containerType().clampContents(container, null);
    }

    @Override
    public boolean isCompatibleWith(NETWORK other) {
        if (super.isCompatibleWith(other)) {
            return container.isEmpty() || other.container.isEmpty() || container.resource().equals(other.container.resource());
        }
        return false;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        if (!container.resource().equals(lastType)) {
            //If the type does not match update it, and mark that we need an update
            if (!container.isEmpty()) {
                lastType = container.resource();
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
                container.setContents(container.stackHelper().empty(), null);
            }
        } else {
            lastType = type;
            container.setContents(type, 1, null);
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
        return MekanismLang.NETWORK_MB_STORED.translate(container.resource(), container.amountAsLong());
    }

    @Override
    public Component getFlowInfo() {
        return MekanismLang.NETWORK_MB_PER_TICK.translate(prevTransferAmount);
    }

    @Override
    protected void onLastTransmitterRemoved(@NotNull TRANSMITTER triggerTransmitter) {
        if (!container.isEmpty()) {
            disperse(triggerTransmitter, container.resource(), container.amountAsLong());
        }
    }

    @Override
    protected void updateSaveShares(@Nullable TRANSMITTER triggerTransmitter, TransactionContext transaction) {
        super.updateSaveShares(triggerTransmitter, transaction);
        if (!isEmpty()) {
            Collection<TRANSMITTER> transmitters = getTransmitters();
            ResourceTransmitterSaveTarget<RESOURCE> saveTarget = new ResourceTransmitterSaveTarget<>(transmitters.size());
            for (TRANSMITTER transmitter : transmitters) {
                saveTarget.addHandler(transmitter.startNewSaveShare(transaction));
            }
            RESOURCE resource = container.resource();
            long toSend = container.amountAsLong();
            long sent = EmitUtils.sendToAcceptors(saveTarget, toSend, resource, transaction);
            if (triggerTransmitter != null && sent < toSend) {
                disperse(triggerTransmitter, resource, toSend - sent);
            }
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
                prevTransferAmount = tickEmit(container.resource(), current, transaction);
                container.setContents(container.resource(), current - prevTransferAmount, transaction);
                transaction.commit();
            }
        }
    }

    private long tickEmit(RESOURCE typeToSend, long amountToSend, TransactionContext transaction) {
        List<ResourceHandler<RESOURCE>> targets = null;
        for (Map<Direction, ResourceHandler<RESOURCE>> acceptors : acceptorCache.getAcceptorValues()) {
            for (ResourceHandler<RESOURCE> acceptor : acceptors.values()) {
                if (targets == null) {
                    //Lazily initialize the list of targets, which allows us to also skip attempting to start emitting
                    targets = new ArrayList<>();
                }
                //Note: We add the target regardless of if we can insert into it, as it skips the extra check,
                // and sendToAcceptors needs to calculate if the target can accept anyway
                targets.add(acceptor);
            }
        }
        return targets == null ? 0 : ResourceUtils.emit(targets, typeToSend, amountToSend, transaction);
    }

    @FunctionalInterface
    protected interface ContainerCreator<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

        CONTAINER create(LongSupplier capacity, BiPredicate<RESOURCE, AutomationType> canExtract, BiPredicate<RESOURCE, AutomationType> canInsert,
              Predicate<RESOURCE> validator, IContentsListener listener);
    }
}