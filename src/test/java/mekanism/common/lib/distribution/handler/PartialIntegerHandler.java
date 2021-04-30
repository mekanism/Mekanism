package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.handler.IntegerHandler;

public class PartialIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        int amountToTake = amountOffered / 2;
        if (!isSimulate) {
            accept(amountToTake);
        }
        return amountToTake;
    }
}