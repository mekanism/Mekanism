package mekanism.common.util;

import java.util.Collection;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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

    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(IResourceContainer<RESOURCE> container, ResourceHandler<RESOURCE> handler,
          AutomationType automationType, @Nullable TransactionContext transaction) {
        RESOURCE resource = container.resource();
        if (resource.isEmpty()) {
            return getTypeToExtract(resource, handler, type -> container.isValidForInsertion(type, automationType), transaction);
        }
        return resource;
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
        return emit(targets, tank, tank.amountAsInt(), transaction);
    }

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          CONTAINER tank, int maxOutput, @Nullable TransactionContext transaction) {
        if (!tank.isEmpty() && maxOutput > 0 && !targets.isEmpty()) {
            RESOURCE resourceType = tank.resource();
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
        return EmitUtils.sendToAcceptors(target, resourceAmount, resourceType, transaction);
    }
}