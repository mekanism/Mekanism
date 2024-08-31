package mekanism.common.lib.distribution.handler;

public class InfiniteLongHandler extends LongHandler {

    @Override
    public long perform(long amountOffered, boolean isSimulate) {
        if (!isSimulate) {
            accept(amountOffered);
        }
        return amountOffered;
    }
}