package mekanism.common.base.target;

import java.util.Iterator;
import java.util.Map.Entry;
import mekanism.common.base.SplitInfo;
import net.minecraft.util.EnumFacing;

/**
 * To be removed and merged into {@link Target} once energy no longer uses doubles.
 */
public abstract class IntegerTypeTarget<HANDLER, EXTRA> extends Target<HANDLER, Integer, EXTRA> {

    @Override
    public Integer sendGivenWithDefault(Integer amountPer) {
        int sent = 0;
        for (Entry<EnumFacing, Integer> giveInfo : given.entrySet()) {
            sent += acceptAmount(giveInfo.getKey(), giveInfo.getValue());
        }
        //If needed is not empty then we default it to the given calculated fair split amount of remaining energy
        for (EnumFacing side : needed.keySet()) {
            sent += acceptAmount(side, amountPer);
        }
        return sent;
    }

    @Override
    public void shiftNeeded(SplitInfo<Integer> splitInfo) {
        Iterator<Entry<EnumFacing, Integer>> iterator = needed.entrySet().iterator();
        //Use an iterator rather than a copy of the keyset of the needed submap
        // This allows for us to remove it once we find it without  having to
        // start looping again or make a large number of copies of the set
        while (iterator.hasNext()) {
            Entry<EnumFacing, Integer> needInfo = iterator.next();
            int amountNeeded = needInfo.getValue();
            int amountPer = splitInfo.getAmountPer();
            //Use compare to?
            if (amountNeeded <= amountPer) {
                addGiven(needInfo.getKey(), amountNeeded);
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