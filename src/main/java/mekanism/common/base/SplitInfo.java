package mekanism.common.base;

import mekanism.api.math.FloatingLong;

public abstract class SplitInfo<TYPE extends Number & Comparable<TYPE>> {

    protected int toSplitAmong;
    public boolean amountPerChanged = false;

    private SplitInfo(int totalTargets) {
        this.toSplitAmong = totalTargets;
    }

    public abstract void send(TYPE amountNeeded);

    public abstract TYPE getAmountPerTarget();

    public abstract TYPE getTotalSent();

    public static class IntegerSplitInfo extends SplitInfo<Integer> {

        private int amountToSplit;
        //AmountPer is the one that needs to be int or double
        private int amountPerTarget;
        private int sentSoFar;

        //Amount to split also should be int or double
        public IntegerSplitInfo(int amountToSplit, int totalTargets) {
            super(totalTargets);
            this.amountToSplit = amountToSplit;
            amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
            sentSoFar = 0;
        }

        @Override
        public void send(Integer amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit -= amountNeeded;
            sentSoFar += amountNeeded;
            toSplitAmong--;
            //Only recalculate it if it is not willing to accept/doesn't want the
            // full per side split
            if (amountNeeded != amountPerTarget && toSplitAmong != 0) {
                int amountPerLast = amountPerTarget;
                amountPerTarget = amountToSplit / toSplitAmong;
                if (!amountPerChanged && amountPerTarget != amountPerLast) {
                    amountPerChanged = true;
                }
            }
        }

        @Override
        public Integer getAmountPerTarget() {
            return amountPerTarget;
        }

        @Override
        public Integer getTotalSent() {
            return sentSoFar;
        }
    }

    public static class FloatingLongSplitInfo extends SplitInfo<FloatingLong> {

        private FloatingLong amountToSplit;
        private FloatingLong amountPerTarget;
        private FloatingLong sentSoFar;

        public FloatingLongSplitInfo(FloatingLong amountToSplit, int totalTargets) {
            super(totalTargets);
            this.amountToSplit = amountToSplit.copy();
            amountPerTarget = toSplitAmong == 0 ? FloatingLong.ZERO : amountToSplit.divide(toSplitAmong);
            sentSoFar = FloatingLong.getNewZero();
        }

        @Override
        public void send(FloatingLong amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit.minusEqual(amountNeeded);
            sentSoFar.plusEqual(amountNeeded);
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
        public FloatingLong getAmountPerTarget() {
            return amountPerTarget;
        }

        @Override
        public FloatingLong getTotalSent() {
            return sentSoFar;
        }
    }
}