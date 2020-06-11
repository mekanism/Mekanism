package mekanism.common.lib.distribution;

import mekanism.api.math.FloatingLong;

public class FloatingLongSplitInfo extends SplitInfo<FloatingLong> {

    private FloatingLong amountToSplit;
    private FloatingLong amountPerTarget;
    private FloatingLong sentSoFar;

    public FloatingLongSplitInfo(FloatingLong amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit.copy();
        amountPerTarget = toSplitAmong == 0 ? FloatingLong.ZERO : amountToSplit.divide(toSplitAmong);
        sentSoFar = FloatingLong.ZERO;
    }

    @Override
    public void send(FloatingLong amountNeeded) {
        //If we are giving it, then lower the amount we are checking/splitting
        amountToSplit = amountToSplit.minusEqual(amountNeeded);
        sentSoFar = sentSoFar.plusEqual(amountNeeded);
        toSplitAmong--;
        //Only recalculate it if it is not willing to accept/doesn't want the
        // full per side split
        if (!amountNeeded.equals(amountPerTarget) && toSplitAmong != 0) {
            FloatingLong amountPerLast = amountPerTarget;
            amountPerTarget = amountToSplit.divide(toSplitAmong);
            if (!amountPerChanged && !amountPerTarget.equals(amountPerLast)) {
                amountPerChanged = true;
            }
        }
    }

    @Override
    public FloatingLong getShareAmount() {
        return amountPerTarget;
    }

    @Override
    public FloatingLong getRemainderAmount() {
        //TODO: Decide if we want to try and adjust for the very small amount that may get lost/be a remainder
        // currently we just ignore it
        return amountPerTarget;
    }

    @Override
    public FloatingLong getTotalSent() {
        return sentSoFar;
    }
}