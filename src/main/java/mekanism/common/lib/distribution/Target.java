package mekanism.common.lib.distribution;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/// Keeps track of a target for emitting from various networks.
///
/// @param <HANDLER>  The Handler this target keeps track of.
/// @param <RESOURCE> The resource being sent. Any amount field in this should be ignored.
public abstract class Target<HANDLER, RESOURCE> {

    /// Collection of handlers
    protected final Collection<HANDLER> handlers;
    /// Collection of handler type pairs that want more than we can/are willing to provide. Value is the amount they want.
    protected final Reference2LongMap<HANDLER> needed;

    protected Target() {
        handlers = new LinkedList<>();
        //TODO: Does this need to be linked?
        needed = new Reference2LongLinkedOpenHashMap<>();
    }

    protected Target(Collection<HANDLER> allHandlers) {
        this.handlers = Collections.unmodifiableCollection(allHandlers);
        this.needed = new Reference2LongLinkedOpenHashMap<>(allHandlers.size() / 2);
    }

    protected Target(int expectedSize) {
        this.handlers = new ArrayList<>(expectedSize);
        this.needed = new Reference2LongLinkedOpenHashMap<>(expectedSize / 2);
    }

    public void addHandler(HANDLER handler) {
        handlers.add(handler);
    }

    public int getHandlerCount() {
        return handlers.size();
    }

    /// @param resource    Any extra information such as chemical or fluid resource.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return The amount that actually got sent.
    public long sendToSingularAcceptor(RESOURCE resource, long toSend, @Nullable TransactionContext transaction) {
        if (handlers.size() != 1) {
            throw new IllegalStateException("Send to singular acceptor should only be called when there is a single handler");
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            HANDLER entry = handlers.iterator().next();
            //Try to send it all to the one handler we actually have, and then just return the amount sent rather than calculating a split
            long accepted = accept(entry, resource, toSend, subTransaction);
            subTransaction.commit();
            return accepted;
        }
    }

    /// @param splitInfo   Information containing the split.
    /// @param resource    Any extra information such as chemical or fluid resource.
    /// @param transaction The transaction that this operation is part of. This method will always use a nested transaction that will be committed. `null` can be passed
    /// to conveniently have this method open its own root transaction and perform the sending.
    ///
    /// @return The amount that actually got sent.
    public long sendToAcceptors(SplitInfo splitInfo, RESOURCE resource, @Nullable TransactionContext transaction) {
        //Short circuit the two cases that we shouldn't be called directly with anyway just in case a caller does call us with them
        if (handlers.isEmpty()) {
            return splitInfo.getTotalSent();
        } else if (handlers.size() == 1) {
            return splitInfo.getTotalSent() + sendToSingularAcceptor(resource, splitInfo.getUnsent(), transaction);
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            //Simulate addition, sending when the requested amount is less than the amountPer
            // splitInfo gets adjusted to account for how much is actually sent
            sendPossible(resource, splitInfo, subTransaction);

            //Only run this if we changed the amountPer from when we first/last ran things
            while (splitInfo.amountPerChanged) {
                splitInfo.amountPerChanged = false;
                //splitInfo gets adjusted to account for how much is actually sent,
                // and if amountPer got changed again, and we need to rerun this
                shiftNeeded(resource, splitInfo, subTransaction);
            }

            //Evenly distribute the remaining amount we have to give between all targets and handlers
            // splitInfo gets adjusted to account for how much is actually sent
            sendRemainingSplit(resource, splitInfo, subTransaction);
            subTransaction.commit();
            return splitInfo.getTotalSent();
        }
    }

    /// Insert into the handler.
    ///
    /// @param handler     The handler (should correspond with the side we are simulating).
    /// @param resource    All the information we are inserting.
    /// @param transaction The transaction that this operation is part of.
    ///
    /// @return The amount it was actually willing to accept.
    protected abstract long accept(HANDLER handler, RESOURCE resource, long amount, TransactionContext transaction);

    /// Calculates how much each handler can take of the resource. If the amount requested is less than the amount per handler/target in splitInfo it immediately commits
    /// the sending.
    ///
    /// @param resource    Any extra information such as chemical or fluid resource.
    /// @param splitInfo   Information about current overall split.
    /// @param transaction The transaction that this operation is part of.
    private void sendPossible(RESOURCE resource, SplitInfo splitInfo, TransactionContext transaction) {
        //TODO: If the share amount is zero so we are all remainder, can we skip the logic that requires adding it to the needed map?
        for (HANDLER entry : handlers) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                long amountNeeded = accept(entry, resource, splitInfo.getUnsent(), subTransaction);
                if (amountNeeded <= splitInfo.getShareAmount()) {
                    //We need less than we were offered, just immediately commit the change
                    splitInfo.send(amountNeeded, true);
                    subTransaction.commit();
                } else {
                    needed.put(entry, amountNeeded);
                }
            }
        }
    }

    /// Rechecks to see if any of the needed amounts is able to fit under the new split and if so gives them the requested amount.
    ///
    /// @param resource    Any extra information such as chemical or fluid resource.
    /// @param splitInfo   The new split to (re)check.
    /// @param transaction The transaction that this operation is part of.
    private void shiftNeeded(RESOURCE resource, SplitInfo splitInfo, TransactionContext transaction) {
        if (needed.isEmpty() || splitInfo.getShareAmount() == 0) {
            return;
        }
        ObjectIterator<Reference2LongMap.Entry<HANDLER>> iterator = Reference2LongMaps.fastIterator(needed);
        //Use an iterator rather than a copy of the keySet of the needed subMap
        // This allows for us to remove it once we find it without  having to
        // start looping again or make a large number of copies of the set
        while (iterator.hasNext()) {
            Reference2LongMap.Entry<HANDLER> needInfo = iterator.next();
            long amountNeeded = needInfo.getLongValue();
            if (amountNeeded <= splitInfo.getShareAmount()) {
                splitInfo.send(accept(needInfo.getKey(), resource, amountNeeded, transaction), true);
                //Remove it as it has now been sent
                iterator.remove();
                //Continue checking things in case we happen to be
                // getting things in a bad order so that we don't recheck
                // the same values many times
            }
        }
    }

    /// Sends the remaining amount to each handler we still have not settled on an amount for. We increment the amount sent in splitInfo as well as adjust the split as
    /// needed if one ends up accepting less than it originally wanted. (The most likely case this would change is with multi-blocks where it may return the same desire
    /// to all connections, but get satisfied by our first connection).
    ///
    /// @param splitInfo Keeps track of the current amount sent and the default each one can get.
    private void sendRemainingSplit(RESOURCE resource, SplitInfo splitInfo, TransactionContext transaction) {
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        if (!needed.isEmpty() && splitInfo.getUnsent() > 0) {
            ObjectIterator<Reference2LongMap.Entry<HANDLER>> iterator = Reference2LongMaps.fastIterator(needed);
            if (needed.size() == 1) {
                //We only have one remaining handler to try and send things to, skip trying to split a single amount between the destinations
                // and just send everything that was unsent to the one handler.
                long accepted = accept(iterator.next().getKey(), resource, splitInfo.getUnsent(), transaction);
                //Note: We don't bother decrementing targets as we are just doing a final pass where we aren't going to query split amounts anymore
                splitInfo.send(accepted, false);
                return;
            }
            while (iterator.hasNext()) {
                long remainderAmount = splitInfo.getRemainderAmount();
                if (remainderAmount == 0) {
                    //We finished inserting everything we wanted to, we can just exit
                    return;
                }
                Reference2LongMap.Entry<HANDLER> needInfo = iterator.next();
                //Accept the remaining amount
                long amountNeeded = needInfo.getLongValue();
                if (amountNeeded <= remainderAmount) {
                    splitInfo.send(accept(needInfo.getKey(), resource, amountNeeded, transaction), true);
                    //If the amount we needed was the less than or the same as our remaining amount
                    // we can remove the value as it has now been sent
                    iterator.remove();
                } else {
                    splitInfo.send(accept(needInfo.getKey(), resource, remainderAmount, transaction), false);
                }
            }
            //TODO: If we remove buffers maybe we should evaluate not caring if we don't actually send the full excess remainder?
            // Given ideally we wouldn't attempting to insert the excess remainder to handlers as a second call to the handler on the same tick
            long unsent = splitInfo.getUnsent();
            if (unsent > 0) {
                //If we still have some of a remainder after trying to evenly distribute the remainder just send it to the first target willing to accept it
                // This might happen if one of the destinations was only able to accept part of the remaining amount, though in general that case will be
                // covered by shifting the needed values
                for (HANDLER recipient : needed.keySet()) {
                    long accepted = accept(recipient, resource, unsent, transaction);
                    //Note: We don't bother decrementing targets as we are just doing a final pass where we aren't going to query split amounts anymore
                    splitInfo.send(accepted, false);
                    unsent -= accepted;
                    if (unsent == 0) {
                        //We finished, exit
                        return;
                    }
                }
            }
        }
    }
}