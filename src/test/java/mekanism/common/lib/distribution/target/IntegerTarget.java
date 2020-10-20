package mekanism.common.lib.distribution.target;

import mekanism.common.lib.distribution.Target;

public abstract class IntegerTarget extends Target<Integer, Integer, Integer> {

    private int accepted;

    protected void accept(int amount) {
        accepted += amount;
    }

    public int getAccepted() {
        return accepted;
    }
}