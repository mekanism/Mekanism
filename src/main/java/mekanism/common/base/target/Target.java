package mekanism.common.base.target;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.EnumFacing;

/**
 * Keeps track of a target for emitting from various networks.
 *
 * @param <HANDLER> The Handler this target keeps track of.
 * @param <TYPE> The type that is being transferred.
 * @param <EXTRA> Any extra information this target may need to keep track of.
 */
public abstract class Target<HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA> {

    /**
     * Map of the sides to the handler for that side.
     */
    protected final Map<EnumFacing, HANDLER> handlers = new EnumMap<>(EnumFacing.class);
    /**
     * Map of sides that want more than we can/are willing to provide. Value is the amount they want.
     */
    protected final Map<EnumFacing, TYPE> needed = new EnumMap<>(EnumFacing.class);

    protected EXTRA extra;

    public void addHandler(EnumFacing side, HANDLER handler) {
        handlers.put(side, handler);
    }

    public Map<EnumFacing, HANDLER> getHandlers() {
        return handlers;
    }

    /**
     * Sends the remaining amount to each handler we still have not settled on an amount for.
     * We increment the amount sent in splitInfo as well as adjust the split as needed if one ends up accepting less
     * than it originally wanted. (The most likely case this would
     *
     * @param splitInfo Keeps track of the current amount sent and the default each one can get.
     */
    public void sendRemainingSplit(SplitInfo<TYPE> splitInfo) {
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (EnumFacing side : needed.keySet()) {
            //Remove the actual amount sent from the split
            acceptAmount(side, splitInfo, splitInfo.getAmountPer());
        }
    }

    /**
     * Gives the handler on the specified side the given amount.
     *
     * @param side Side of handler to give.
     * @param splitInfo Sent amount is incremented by the amount actually given.
     * @param amount Amount to give.
     */
    protected abstract void acceptAmount(EnumFacing side, SplitInfo<TYPE> splitInfo, TYPE amount);

    protected abstract TYPE simulate(HANDLER handler, EnumFacing side, EXTRA extra);

    public void simulate(EXTRA toSend, SplitInfo<TYPE> splitInfo) {
        for (Entry<EnumFacing, HANDLER> entry : handlers.entrySet()) {
            TYPE amountNeeded = simulate(entry.getValue(), entry.getKey(), toSend);
            if (amountNeeded.compareTo(splitInfo.getAmountPer()) <= 0) {
                //Add the amount, in case something changed from simulation only mark actual sent amount
                // in split info
                acceptAmount(entry.getKey(), splitInfo, amountNeeded);
            } else {
                needed.put(entry.getKey(), amountNeeded);
            }
        }
    }

    public void shiftNeeded(SplitInfo<TYPE> splitInfo) {
        Iterator<Entry<EnumFacing, TYPE>> iterator = needed.entrySet().iterator();
        //Use an iterator rather than a copy of the keyset of the needed submap
        // This allows for us to remove it once we find it without  having to
        // start looping again or make a large number of copies of the set
        while (iterator.hasNext()) {
            Entry<EnumFacing, TYPE> needInfo = iterator.next();
            TYPE amountNeeded = needInfo.getValue();
            if (amountNeeded.compareTo(splitInfo.getAmountPer()) <= 0) {
                acceptAmount(needInfo.getKey(), splitInfo, amountNeeded);
                //Remove it as it has now been sent
                iterator.remove();
                //Continue checking things in case we happen to be
                // getting things in a bad order so that we don't recheck
                // the same values many times
            }
        }
    }
}