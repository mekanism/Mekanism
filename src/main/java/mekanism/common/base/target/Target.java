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
 * @implNote Eventually when/if we do away with Joules this will be able to be converted to having TYPE always be an
 * Integer. We will then be able to use the primitive type in various places getting rid of the need for
 * IntegerTypeTarget, and having the {@link #sendGivenWithDefault(Number, Number)} be implemented directly here. This
 * can then be simplified as well as having SplitInfo be simplified down to using primitive integers.
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
    /**
     * Map of sides to how much we are giving them, this will be less than or equal to our offered split.
     */
    protected final Map<EnumFacing, TYPE> given = new EnumMap<>(EnumFacing.class);

    protected EXTRA extra;

    public void addHandler(EnumFacing side, HANDLER handler) {
        handlers.put(side, handler);
    }

    public Map<EnumFacing, HANDLER> getHandlers() {
        return handlers;
    }

    /**
     * @param side Side to add the amount to.
     * @param amountNeeded Amount that side needs.
     * @param canGive True if we are willing to give that side the full amount, then add it to the given map, otherwise
     * add it to the needed map.
     */
    public void addAmount(EnumFacing side, TYPE amountNeeded, boolean canGive) {
        if (canGive) {
            given.put(side, amountNeeded);
        } else {
            needed.put(side, amountNeeded);
        }
    }

    /**
     * Sends the calculated amount in given to each handler. If we failed to calculate a given amount for any handler in
     * this target send the given default instead.
     *
     * @param current The current amount of power having been given.
     * @param amountPer Default amount per handler in this target.
     * @return Actual total amount sent to this target added to the current amount
     */
    public abstract TYPE sendGivenWithDefault(TYPE current, TYPE amountPer);

    /**
     * Gives the handler on the specified side the given amount.
     *
     * @param side Side of handler to give.
     * @param amount Amount to give.
     * @return Amount actually taken.
     */
    protected abstract TYPE acceptAmount(EnumFacing side, TYPE amount);

    protected abstract TYPE simulate(HANDLER handler, EnumFacing side, EXTRA extra);

    public void simulate(EXTRA toSend, SplitInfo<TYPE> splitInfo) {
        for (Entry<EnumFacing, HANDLER> entry : handlers.entrySet()) {
            TYPE amountNeeded = simulate(entry.getValue(), entry.getKey(), toSend);
            boolean canGive = amountNeeded.compareTo(splitInfo.getAmountPer()) <= 0;
            addAmount(entry.getKey(), amountNeeded, canGive);
            //Add the amount
            if (canGive) {
                splitInfo.remove(amountNeeded);
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
                given.put(needInfo.getKey(), amountNeeded);
                //Remove it as it no longer valid
                iterator.remove();
                //Remove this amount from the split calculation
                splitInfo.remove(amountNeeded);
                //Continue checking things in case we happen to be
                // getting things in a bad order so that we don't recheck
                // the same values many times
            }
        }
    }
}