package mekanism.common.lib.distribution.target;

import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.IntegerHandler;

public final class IntegerTarget extends Target<IntegerHandler, Integer, Integer> {

    @Override
    protected void acceptAmount(IntegerHandler integerHandler, SplitInfo<Integer> splitInfo, Integer amount) {
        splitInfo.send(integerHandler.perform(amount, false));
    }

    @Override
    protected Integer simulate(IntegerHandler integerHandler, Integer offered) {
        return integerHandler.perform(offered, true);
    }
}