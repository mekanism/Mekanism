package mekanism.common.base;

public abstract class SplitInfo<TYPE extends Number & Comparable<TYPE>> {

    protected int toSplitAmong;
    public boolean amountPerChanged = false;

    private SplitInfo(int totalTargets) {
        this.toSplitAmong = totalTargets;
    }

    public abstract void remove(TYPE amountNeeded);

    public abstract TYPE getAmountPer();

    public static class IntegerSplitInfo extends SplitInfo<Integer> {
        private int amountToSplit;
        //AmountPer is the one that needs to be int or double
        private int amountPer;

        //Amount to split also should be int or double
        public IntegerSplitInfo(int amountToSplit, int totalTargets) {
            super(totalTargets);
            this.amountToSplit = amountToSplit;
            this.amountPer = amountToSplit / toSplitAmong;
        }

        @Override
        public void remove(Integer amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit -= amountNeeded;
            toSplitAmong--;
            //Only recalculate it if it is not willing to accept/doesn't want the
            // full per side split
            if (amountNeeded != amountPer && toSplitAmong != 0) {
                int amountPerLast = amountPer;
                amountPer = amountToSplit / toSplitAmong;
                if (!amountPerChanged && amountPer != amountPerLast) {
                    amountPerChanged = true;
                }
            }
        }

        @Override
        public Integer getAmountPer() {
            return amountPer;
        }
    }

    public static class DoubleSplitInfo extends SplitInfo<Double> {
        private double amountToSplit;
        private double amountPer;

        public DoubleSplitInfo(double amountToSplit, int totalTargets) {
            super(totalTargets);
            this.amountToSplit = amountToSplit;
            this.amountPer = amountToSplit / toSplitAmong;
        }

        @Override
        public void remove(Double amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit -= amountNeeded;
            toSplitAmong--;
            //Only recalculate it if it is not willing to accept/doesn't want the
            // full per side split
            if (amountNeeded != amountPer && toSplitAmong != 0) {
                double amountPerLast = amountPer;
                amountPer = amountToSplit / toSplitAmong;
                if (!amountPerChanged && amountPer != amountPerLast) {
                    amountPerChanged = true;
                }
            }
        }

        @Override
        public Double getAmountPer() {
            return amountPer;
        }
    }
}