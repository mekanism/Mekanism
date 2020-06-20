package mekanism.common.lib.distribution;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Direction;

/**
 * Keeps track of a target for emitting from various networks.
 *
 * @param <HANDLER> The Handler this target keeps track of.
 * @param <TYPE>    The type that is being transferred.
 * @param <EXTRA>   Any extra information this target may need to keep track of.
 */
public abstract class Target<HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA> {

    /**
     * Map of the sides to the handler for that side.
     */
    protected final Map<Direction, HANDLER> handlers = new EnumMap<>(Direction.class);
    /**
     * Map of sides that want more than we can/are willing to provide. Value is the amount they want.
     */
    protected final Map<Direction, TYPE> needed = new EnumMap<>(Direction.class);

    protected EXTRA extra;

    public void addHandler(Direction side, HANDLER handler) {
        handlers.put(side, handler);
    }

    public Map<Direction, HANDLER> getHandlers() {
        return handlers;
    }

    /**
     * Sends the remaining amount to each handler we still have not settled on an amount for. We increment the amount sent in splitInfo as well as adjust the split as
     * needed if one ends up accepting less than it originally wanted. (The most likely case this would change is with multi-blocks where it may return the same desire to
     * all connections, but get satisfied by our first connection).
     *
     * @param splitInfo Keeps track of the current amount sent and the default each one can get.
     */
    public void sendRemainingSplit(SplitInfo<TYPE> splitInfo) {
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (Direction side : needed.keySet()) {
            acceptAmount(handlers.get(side), splitInfo, splitInfo.getRemainderAmount());
        }
    }

    /**
     * Gives the handler on the specified side the given amount.
     *
     * @param handler   Handler to give to.
     * @param splitInfo Information about current overall split. The given split will be increased by the actual amount accepted, in case it is less than the offered
     *                  amount.
     * @param amount    Amount to give.
     *
     * @implNote Must call {@link SplitInfo#send(Number)} with the amount actually accepted.
     */
    protected abstract void acceptAmount(HANDLER handler, SplitInfo<TYPE> splitInfo, TYPE amount);

    /**
     * Simulate inserting into the handler.
     *
     * @param handler The handler (should correspond with the side we are simulating).
     * @param extra   All the information we are inserting.
     *
     * @return The amount it was actually willing to accept.
     */
    protected abstract TYPE simulate(HANDLER handler, EXTRA extra);

    /**
     * Calculates how much each handler can take of toSend. If the amount requested is less than the amount per handler/target in splitInfo it immediately sends the
     * requested amount to the handler via {@link #acceptAmount(HANDLER, SplitInfo, Number)}
     *
     * @param toSend    The total amount getting sent.
     * @param splitInfo Information about current overall split.
     */
    public void sendPossible(EXTRA toSend, SplitInfo<TYPE> splitInfo) {
        for (Entry<Direction, HANDLER> entry : handlers.entrySet()) {
            TYPE amountNeeded = simulate(entry.getValue(), toSend);
            if (amountNeeded.compareTo(splitInfo.getShareAmount()) <= 0) {
                //Add the amount, in case something changed from simulation only mark actual sent amount
                // in split info
                acceptAmount(entry.getValue(), splitInfo, amountNeeded);
            } else {
                needed.put(entry.getKey(), amountNeeded);
            }
        }
    }

    /**
     * Rechecks to see if any of the needed amounts is able to fit under the new split and if so gives them the requested amount.
     *
     * @param splitInfo The new split to (re)check.
     */
    public void shiftNeeded(SplitInfo<TYPE> splitInfo) {
        Iterator<Entry<Direction, TYPE>> iterator = needed.entrySet().iterator();
        //Use an iterator rather than a copy of the keySet of the needed subMap
        // This allows for us to remove it once we find it without  having to
        // start looping again or make a large number of copies of the set
        while (iterator.hasNext()) {
            Entry<Direction, TYPE> needInfo = iterator.next();
            TYPE amountNeeded = needInfo.getValue();
            if (amountNeeded.compareTo(splitInfo.getShareAmount()) <= 0) {
                acceptAmount(handlers.get(needInfo.getKey()), splitInfo, amountNeeded);
                //Remove it as it has now been sent
                iterator.remove();
                //Continue checking things in case we happen to be
                // getting things in a bad order so that we don't recheck
                // the same values many times
            }
        }
    }
}