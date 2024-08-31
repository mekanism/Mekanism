package mekanism.common.lib.distribution.target;

import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.LongHandler;

public final class LongTarget extends Target<LongHandler, Void> {

    @Override
    protected void acceptAmount(LongHandler integerHandler, SplitInfo splitInfo, Void resource, long amount) {
        splitInfo.send(integerHandler.perform(MathUtils.clampToInt(amount), false));
    }

    @Override
    protected long simulate(LongHandler integerHandler, Void resource, long amount) {
        return integerHandler.perform(MathUtils.clampToInt(amount), true);
    }
}