package mekanism.common.util;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Docs
public final class ResourceUtils {

    private ResourceUtils() {
    }

    public static <RESOURCE extends Resource> int extractManual(ResourceHandler<RESOURCE> handler, RESOURCE resource, int amount, TransactionContext transaction) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.extract(resource, amount, transaction, AutomationType.MANUAL);
        }
        return handler.extract(resource, amount, transaction);
    }

    public static <RESOURCE extends Resource> int insertManual(ResourceHandler<RESOURCE> handler, RESOURCE resource, int amount, TransactionContext transaction) {
        if (handler instanceof IMekanismResourceHandler<RESOURCE, ?> mekHandler) {
            //Ensure droppers use the manual automation type
            return mekHandler.insert(resource, amount, transaction, AutomationType.MANUAL);
        }
        return handler.insert(resource, amount, transaction);
    }

    //TODO - 26.1: validate and then add as docs that we don't need to also be modifying toAdd
    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> void merge(@NotNull List<CONTAINER> orig, @NotNull List<CONTAINER> toAdd,
          Object2LongMap<RESOURCE> rejects, TransactionContext transaction) {
        StorageUtils.validateSizeMatches(orig, toAdd, "container");
        for (int container = 0, size = toAdd.size(); container < size; container++) {
            CONTAINER toAddContainer = toAdd.get(container);
            if (!toAddContainer.isEmpty()) {
                RESOURCE toAddResource = toAddContainer.getResource();
                long toAddAmount = toAddContainer.amountAsLong();
                CONTAINER origContainer = orig.get(container);
                //TODO - 26.1: Validate all callers have this work with the given automation type
                // Also how much do we care about merging identical slots? Should we use the InventoryUtils#insertItem helper
                // to try inserting against all the slots of the other?
                //TODO - 26.1: Is  this how we want to handle trying to insert it, or would it be better to basically loop inserting multiple times as long
                // as we are inserting max int while we get closer to toAddAmount
                int added = origContainer.insert(toAddResource, Ints.saturatedCast(toAddAmount), transaction, AutomationType.INTERNAL);
                if (added < toAddAmount) {
                    //Add any remainder to the rejects
                    rejects.mergeLong(toAddResource, toAddAmount - added, Long::sum);
                }
            }
        }
    }

    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(IResourceContainer<RESOURCE> container, ResourceHandler<RESOURCE> handler,
          AutomationType automationType, @Nullable TransactionContext transaction) {
        return getTypeToExtract(container, handler, type -> container.isValidForInsertion(type, automationType), transaction);
    }

    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(IResourceContainer<RESOURCE> container, ResourceHandler<RESOURCE> handler,
          Predicate<RESOURCE> filter, @Nullable TransactionContext transaction) {
        return getTypeToExtract(container.getResource(), handler, filter, transaction);
    }

    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(RESOURCE type, ResourceHandler<RESOURCE> handler, Predicate<RESOURCE> filter, @Nullable TransactionContext transaction) {
        if (type.isEmpty()) {
            RESOURCE extractableType = ResourceHandlerUtil.findExtractableResource(handler, filter, transaction);
            if (extractableType != null) {
                return extractableType;
            }
        }
        return type;
    }

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          CONTAINER tank, @Nullable TransactionContext transaction) {
        return emit(targets, tank, tank.capacityAsInt(tank.getResource()), transaction);
    }

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          CONTAINER tank, int maxOutput, @Nullable TransactionContext transaction) {
        if (!tank.isEmpty() && maxOutput > 0 && !targets.isEmpty()) {
            RESOURCE resourceType = tank.getResource();
            int resourceAmount;
            try (Transaction simulation = Transaction.open(transaction)) {
                resourceAmount = tank.extract(resourceType, maxOutput, simulation, AutomationType.INTERNAL);
                if (resourceAmount == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //We won't be able to extract the resource, just fail early
                int sent = emit(targets, resourceType, resourceAmount, subTransaction);
                if (tank.extract(resourceType, sent, subTransaction, AutomationType.INTERNAL) == sent) {
                    //Validate that we were able to extract the amount we sent. In theory this should always be true
                    subTransaction.commit();
                    return sent;
                }
            }
        }
        return 0;
    }

    /**
     * Emits fluid from a central block by splitting the received stack among the sides given.
     *
     * @param targets - the list of capabilities to output to
     * @param stack   - the stack to output
     *
     * @return the amount of fluid emitted
     */
    public static <RESOURCE extends Resource> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets, RESOURCE resourceType,
          int resourceAmount, @Nullable TransactionContext transaction) {
        if (resourceType.isEmpty() || targets.isEmpty() || resourceAmount <= 0) {
            return 0;
        }
        ResourceHandlerTarget<RESOURCE> target = null;
        for (BlockCapabilityCache<ResourceHandler<RESOURCE>, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            ResourceHandler<RESOURCE> handler = capability.getCapability();
            if (handler != null) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (handler.insert(resourceType, resourceAmount, simulation) > 0) {
                        if (target == null) {
                            target = new ResourceHandlerTarget<>(targets.size());
                        }
                        target.addHandler(handler);
                    }
                }
            }
        }
        if (target == null) {
            return 0;
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            int sent = EmitUtils.sendToAcceptors(target, resourceAmount, resourceType, subTransaction);
            subTransaction.commit();
            return sent;
        }
    }

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> void clampContents(CONTAINER container) {
        RESOURCE resource = container.getResource();
        if (!resource.isEmpty()) {
            long capacity = container.capacityAsLong(resource);
            if (capacity == 0 && (container instanceof VariableCapacityFluidTank || container instanceof VariableCapacityChemicalTank)) {
                //TODO - 26.1: Re-evaluate this, and add comments
                //Our capacity should never actually be zero, and given we fake it being zero
                // until we finish building the network, we need to override this method to bypass the upper limit check
                // when our upper limit is zero
                return;
            }
            if (container.amountAsLong() > capacity) {
                container.setContentsUnchecked(resource, capacity);
            }
        }
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     *
     * @see ResourceHandlerUtil#getRedstoneSignalFromResourceHandler(ResourceHandler)
     */
    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> int getRedstoneSignalFromContainers(List<CONTAINER> containers) {
        float proportion = 0.0F;
        int sampleCount = 0; // Number of samples in proportion
        for (CONTAINER container : containers) {
            long containerFill = container.amountAsLong();
            if (containerFill > 0) {
                long capacity = container.capacityAsLong(container.getResource());
                if (capacity > 0) {
                    // Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                    proportion += Math.min(1.0f, (float) containerFill / capacity);
                    sampleCount++;
                }
            }
        }
        if (sampleCount == 0) {
            return Redstone.SIGNAL_NONE;
        }
        //TODO - 26.1: This ignores empty slots? I think that is wrong, even though it is like ResourceHandlerUtil...
        // Vanilla's getRedstoneSignalFromContainer takes the container size
        proportion /= sampleCount;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    public static <RESOURCE extends Resource> int getRedstoneSignalFromContainer(IResourceContainer<RESOURCE> container) {
        long containerFill = container.amountAsLong();
        if (containerFill > 0) {
            long capacity = container.capacityAsLong(container.getResource());
            if (capacity > 0) {
                // Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                float proportion = Math.min(1.0f, (float) containerFill / capacity);
                return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
            }
        }
        return Redstone.SIGNAL_NONE;
    }
}