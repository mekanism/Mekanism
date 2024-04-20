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
        boolean recalculate;
        if (amountNeeded.isZero()) {
            if (!decrementTargets) {
                //If we are not decrementing targets, then don't remove that as a valid target, or update how much there is per target
                return;
            }
            recalculate = true;
        } else {
            amountToSplit = amountToSplit.minusEqual(amountNeeded);
            sentSoFar = sentSoFar.plusEqual(amountNeeded);
            if (!decrementTargets) {
                //If we are not decrementing targets, then don't remove that as a valid target, or update how much there is per target
                return;
            }
            recalculate = !amountNeeded.equals(amountPerTarget);
        }
        toSplitAmong--;
        //Only recalculate it if it is not willing to accept/doesn't want the
        // full per side split
        if (recalculate && toSplitAmong != 0) {
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
    public FloatingLong getUnsent() {
        return amountToSplit;
    }

    @Override
    public boolean isZero(FloatingLong value) {
        return value.isZero();
    }

    @Override
    public FloatingLong getTotalSent() {
        return sentSoFar;
    }
}