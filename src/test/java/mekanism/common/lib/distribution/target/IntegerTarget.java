package mekanism.common.lib.distribution.target;

import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.IntegerHandler;

public final class IntegerTarget extends Target<IntegerHandler, Integer> {

    @Override
    protected void acceptAmount(IntegerHandler integerHandler, SplitInfo splitInfo, long amount) {
        splitInfo.send(integerHandler.perform(MathUtils.clampToInt(amount), false));
    }

    @Override
    protected long simulate(IntegerHandler integerHandler, Integer offered) {
        return integerHandler.perform(offered, true);
    }
}