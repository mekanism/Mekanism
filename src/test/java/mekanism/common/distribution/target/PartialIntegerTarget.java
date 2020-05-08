package mekanism.common.distribution.target;

import mekanism.common.distribution.SplitInfo;

public class PartialIntegerTarget extends Target<Integer, Integer, Integer> {

    @Override
    protected void acceptAmount(Integer handler, SplitInfo<Integer> splitInfo, Integer amount) {
        //Mark that we accepted half of it
        splitInfo.send(amount / 2);
    }

    @Override
    protected Integer simulate(Integer handler, Integer toFill) {
        //Pretend we could accept half of it
        return toFill / 2;
    }
}