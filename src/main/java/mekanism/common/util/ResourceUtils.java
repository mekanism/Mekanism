package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.content.network.distribution.ResourceHandlerTarget;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class ResourceUtils {

    private ResourceUtils() {
    }

    /// Gets the current type of resource stored in the container, or if it is empty, gets the first resource that can be extracted from the given handler that is valid
    /// for insertion into the container.
    ///
    /// @param container   The container to get the current type of, and check if types are valid for insertion.
    /// @param handler     The handler to check for resources.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be rolled back. `null` can be passed
    /// to conveniently have this method open its own root transaction.
    ///
    /// @return The resource stored in the container if it is non-empty, or if there was no resource that could be extracted from the handler that is valid for insertion
    /// into the container.
    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(IResourceContainer<RESOURCE> container, ResourceHandler<RESOURCE> handler,
          AutomationType automationType, @Nullable TransactionContext transaction) {
        RESOURCE resource = container.resource();
        if (resource.isEmpty()) {
            //Skip creating the capturing lambda if the resource isn't empty
            return getTypeToExtract(resource, handler, type -> container.isValidForInsertion(type, automationType), transaction);
        }
        return resource;
    }

    /// Checks if the given resource handler has at least one resource that matches the given filter and can be extracted.
    ///
    /// @param handler     The handler to check for resources.
    /// @param filter      A filter that will be applied to each non-empty resource in the handler. Only resources for which this filter returns `true` will be
    /// considered.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be rolled back. `null` can be passed
    /// to conveniently have this method open its own root transaction.
    ///
    /// @return The passed in resource if it is non-empty, or if there was no resource that could be extracted from the handler that matches the filter.
    public static <RESOURCE extends Resource> RESOURCE getTypeToExtract(RESOURCE type, ResourceHandler<RESOURCE> handler, Predicate<RESOURCE> filter, @Nullable TransactionContext transaction) {
        if (type.isEmpty()) {
            RESOURCE extractableType = ResourceHandlerUtil.findExtractableResource(handler, filter, transaction);
            if (extractableType != null) {
                return extractableType;
            }
        }
        return type;
    }

    /// Emits a resource from the given container split among the given collection of targets.
    ///
    /// @param targets     Capability caches to output to.
    /// @param container   Container to transfer resources out of.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of resource transferred out of the container and emitted among the given targets.
    public static <RESOURCE extends Resource> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          IResourceContainer<RESOURCE> container, @Nullable TransactionContext transaction) {
        return emit(targets, container, container.amountAsInt(), transaction);
    }

    /// Emits a resource from the given container at the specified maximum transfer rate split among the given collection of targets.
    ///
    /// @param targets     Capability caches to output to.
    /// @param container   Container to transfer resources out of.
    /// @param maxOutput   Maximum transfer rate to transfer out of the container.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of resource transferred out of the container and emitted among the given targets.
    public static <RESOURCE extends Resource> int emit(Collection<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> targets,
          IResourceContainer<RESOURCE> container, int maxOutput, @Nullable TransactionContext transaction) {
        if (!container.isEmpty() && maxOutput > 0 && !targets.isEmpty()) {
            RESOURCE resourceType = container.resource();
            int resourceAmount;
            try (Transaction simulation = Transaction.open(transaction)) {
                resourceAmount = container.extract(resourceType, maxOutput, simulation, AutomationType.INTERNAL);
                if (resourceAmount == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                //We won't be able to extract the resource, just fail early
                int sent = Ints.saturatedCast(emit(EmitUtils.getHandlersFromCaches(targets), resourceType, resourceAmount, subTransaction));
                if (sent > 0 && container.extract(resourceType, sent, subTransaction, AutomationType.INTERNAL) == sent) {
                    //Validate that we were able to extract the amount we sent. In theory this should always be true
                    subTransaction.commit();
                    return sent;
                }
            }
        }
        return 0;
    }

    /// Emits a resource and splits the amount to a given collection of targets.
    ///
    /// @param targets        Targets to output to.
    /// @param resourceType   Resource type to emit.
    /// @param resourceAmount Amount of the resource to split between the various targets.
    /// @param transaction    The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be
    /// passed to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of resource emitted
    public static <RESOURCE extends Resource> long emit(List<ResourceHandler<RESOURCE>> targets, RESOURCE resourceType, long resourceAmount, @Nullable TransactionContext transaction) {
        if (targets.isEmpty() || resourceType.isEmpty() || resourceAmount <= 0) {
            return 0;
        } else if (targets.size() == 1) {
            //If we only have a single target, optimize out wrapping it in a resource handler target
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int sent = targets.getFirst().insert(resourceType, Ints.saturatedCast(resourceAmount), subTransaction);
                subTransaction.commit();
                return sent;
            }
        }
        return EmitUtils.sendToAcceptors(new ResourceHandlerTarget<>(targets), resourceAmount, resourceType, transaction);
    }

    /// Emits a resource from the given container to the given target at the specified maximum transfer rate.
    ///
    /// @param target      Resource handler to output to.
    /// @param container   Container to transfer resources out of.
    /// @param maxOutput   Maximum transfer rate to transfer out of the container.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return the amount of resource transferred out of the container and emitted to the given target.
    public static <RESOURCE extends Resource> int emit(@Nullable ResourceHandler<RESOURCE> target, IResourceContainer<RESOURCE> container, int maxOutput,
          @Nullable TransactionContext transaction) {
        if (target != null && !container.isEmpty() && maxOutput > 0) {
            RESOURCE resourceType = container.resource();
            int resourceAmount;
            try (Transaction simulation = Transaction.open(transaction)) {
                resourceAmount = container.extract(resourceType, maxOutput, simulation, AutomationType.INTERNAL);
                if (resourceAmount == 0) {
                    //If we failed to extract from it, just exit early
                    return 0;
                }
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                int sent = target.insert(resourceType, resourceAmount, subTransaction);
                if (sent > 0 && container.extract(resourceType, sent, subTransaction, AutomationType.INTERNAL) == sent) {
                    //Validate that we were able to extract the amount we sent. In theory this should always be true
                    subTransaction.commit();
                    return sent;
                }
            }
        }
        return 0;
    }

    /// Simplified version for chemicals of [net.neoforged.neoforge.transfer.fluid.FluidUtil#interactWithFluidHandler]
    public static <RESOURCE extends Resource> boolean interactWithHandler(Player player, InteractionHand hand, ResourceHandler<RESOURCE> handler,
          MultiTypeCapability<ResourceHandler<RESOURCE>> capability, @Nullable TransactionContext transaction) {
        ItemAccess itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        ResourceHandler<RESOURCE> handHandler = capability.getCapability(itemAccess);
        if (handHandler == null) {
            return false;
        }
        return ResourceHandlerUtil.moveFirst(handler, handHandler, ConstantPredicates.alwaysTrue(), Integer.MAX_VALUE, transaction) != null ||
               ResourceHandlerUtil.moveFirst(handHandler, handler, ConstantPredicates.alwaysTrue(), Integer.MAX_VALUE, transaction) != null;
    }
}