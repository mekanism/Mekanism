package mekanism.common.lib.distribution.target;

import com.google.common.primitives.Ints;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import mekanism.common.lib.distribution.handler.IntegerHandler;

public final class IntegerTarget extends Target<IntegerHandler, Void> {

    @Override
    protected void acceptAmount(IntegerHandler integerHandler, SplitInfo splitInfo, Void resource, long amount) {
        splitInfo.send(integerHandler.perform(Ints.saturatedCast(amount), false));
    }

    @Override
    protected long simulate(IntegerHandler integerHandler, Void resource, long amount) {
        return integerHandler.perform(Ints.saturatedCast(amount), true);
    }
}