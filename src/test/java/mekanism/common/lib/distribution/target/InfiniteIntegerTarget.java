package mekanism.common.lib.distribution.target;

import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;

public class InfiniteIntegerTarget extends Target<Integer, Integer, Integer> {

    @Override
    protected void acceptAmount(Integer handler, SplitInfo<Integer> splitInfo, Integer amount) {
        //Mark that we accepted it all
        splitInfo.send(amount);
    }

    @Override
    protected Integer simulate(Integer handler, Integer toFill) {
        //Pretend we could accept it all
        return toFill;
    }
}