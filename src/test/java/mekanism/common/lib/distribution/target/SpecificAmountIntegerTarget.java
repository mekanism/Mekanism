package mekanism.common.lib.distribution.target;

import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class SpecificAmountIntegerTarget extends Target<Integer, Integer, Integer> {

    private int toAccept;

    public SpecificAmountIntegerTarget(int toAccept) {
        this.toAccept = toAccept;
    }

    @Override
    protected void acceptAmount(Integer handler, SplitInfo<Integer> splitInfo, Integer amount) {
        int accepting = Math.min(toAccept, amount);
        if (accepting > 0) {
            splitInfo.send(accepting);
            toAccept -= accepting;
        }
    }

    @Override
    protected Integer simulate(Integer handler, Integer toFill) {
        return Math.min(toAccept, toFill);
    }
}