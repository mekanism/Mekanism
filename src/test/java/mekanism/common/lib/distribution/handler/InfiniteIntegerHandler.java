package mekanism.common.lib.distribution.handler;

import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.handler.IntegerHandler;

public class InfiniteIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        if (!isSimulate) {
            accept(amountOffered);
        }
        return amountOffered;
    }
}