package mekanism.common.util;

import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.Collection;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.container.IMekanismResourceHandler;
import mekanism.api.container.IResourceContainer;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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

    public static <RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          RESOURCE resourceType, int resourceAmount, @UnknownNullability CONTAINER tank, int maxOutput) {
        if (resourceType.isEmpty() != (resourceAmount == 0)) {
            //Something went wrong in calling this method
            //TODO - 26.1: Do we want to log a warning or throw an illegal argument exception?
            return 0;
        } else if (targets.isEmpty()) {
            return 0;
        } else if (resourceType.isEmpty()) {
            if (tank == null) {
                //Something went wrong in calling this method
                return 0;
            }
            resourceType = tank.getResource();
            if (resourceType.isEmpty()) {
                //We won't be able to extract the resource, just fail early
                return 0;
            }
        }
        ResourceHandlerTarget<RESOURCE> target = null;
        for (BlockCapabilityCache<ResourceHandler<RESOURCE>, Direction> capability : targets) {
            //Insert to access side and collect the cap if it is present, and we can insert the type of the stack into it
            ResourceHandler<RESOURCE> handler = capability.getCapability();
            if (handler != null) {
                //If we weren't given a stack by the caller, then we want to lazily try to extract from the tank to see how much we are trying to emit
                // so that we don't have to attempt an extraction if all our targets are actually not currently fluid handlers
                //TODO - 26.1: Update comment because we do partially initialize it (namely we initialize the type)
                if (resourceAmount == 0) {
                    //TODO - 26.1: Check callers as they might be in a transaction context
                    try (Transaction simulation = Transaction.openRoot()) {
                        resourceAmount = tank.extract(resourceType, maxOutput, simulation, AutomationType.INTERNAL);
                        if (resourceAmount == 0) {
                            //If we failed to extract from it, just exit early
                            return 0;
                        }
                    }
                }
                //TODO - 26.1: Check callers as they might be in a transaction context, and can this and the lazy amount share a simulation context?
                try (Transaction simulation = Transaction.openRoot()) {
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
        //TODO - 26.1: Check callers as they might be in a transaction context
        try (Transaction transaction = Transaction.openRoot()) {
            int sent = EmitUtils.sendToAcceptors(target, resourceAmount, resourceType, transaction);
            transaction.commit();
            return sent;
        }
    }
}