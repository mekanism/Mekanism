package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class EmitUtils {

    private EmitUtils() {
    }

    /// @param <HANDLER>        The handler of our target.
    /// @param <RESOURCE>       Type of resource (e.g. Stack). Stack amounts ignored
    /// @param <TARGET>         The emitter target
    /// @param availableTargets The targets to distribute toSend fairly among.
    /// @param amountToSplit    The amount to split between all the targets
    /// @param resource         Any extra information such as chemical or fluid resource.
    /// @param transaction      The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be
    /// passed to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return The amount that actually got sent.
    public static <HANDLER, RESOURCE extends @Nullable Object, TARGET extends Target<HANDLER, RESOURCE>> int sendToAcceptors(@Nullable TARGET availableTargets,
          int amountToSplit, RESOURCE resource, @Nullable TransactionContext transaction) {
        return Ints.saturatedCast(sendToAcceptors(availableTargets, (long) amountToSplit, resource, transaction));
    }

    /// @param <HANDLER>        The handler of our target.
    /// @param <RESOURCE>       Type of resource (e.g. Stack). Stack amounts ignored
    /// @param <TARGET>         The emitter target
    /// @param availableTargets The targets to distribute toSend fairly among.
    /// @param amountToSplit    The amount to split between all the targets
    /// @param resource         Any extra information such as chemical or fluid resource.
    /// @param transaction      The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be
    /// passed to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return The amount that actually got sent.
    public static <HANDLER, RESOURCE extends @Nullable Object, TARGET extends Target<HANDLER, RESOURCE>> long sendToAcceptors(@Nullable TARGET availableTargets,
          long amountToSplit, RESOURCE resource, @Nullable TransactionContext transaction) {
        if (availableTargets == null || amountToSplit == 0) {
            return 0;
        }
        int handlerCount = availableTargets.getHandlerCount();
        if (handlerCount == 0) {
            return 0;
        } else if (handlerCount == 1) {
            //Skip creating the split info for the trivial case
            return availableTargets.sendToSingularAcceptor(resource, amountToSplit, transaction);
        }
        return availableTargets.sendToAcceptors(new SplitInfo(amountToSplit, handlerCount), resource, transaction);
    }

    /// Converts a given collection of capability caches to a list of handlers.
    public static <HANDLER> List<HANDLER> getHandlersFromCaches(Collection<? extends BlockCapabilityCache<? extends HANDLER, ?>> caches) {
        if (caches.isEmpty()) {
            return Collections.emptyList();
        }
        //Note: We add the target regardless of if we can insert into it, as it skips the extra check,
        // and sendToAcceptors needs to calculate if the target can accept anyway
        //TODO: If this ends up being a performance impact, lazy init the list and
        return caches.stream().<HANDLER>map(BlockCapabilityCache::getCapability).filter(Objects::nonNull).toList();
    }
}