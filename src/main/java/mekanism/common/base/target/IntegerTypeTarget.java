package mekanism.common.base.target;

import java.util.Map.Entry;
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
}