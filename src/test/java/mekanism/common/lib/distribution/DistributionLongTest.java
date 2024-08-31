package mekanism.common.lib.distribution;

import java.util.Locale;
import java.util.function.Supplier;
import mekanism.common.lib.distribution.handler.LongHandler;
import mekanism.common.lib.distribution.handler.PartialLongHandler;
import mekanism.common.lib.distribution.handler.InfiniteLongHandler;
import mekanism.common.lib.distribution.handler.LyingAmountLongHandler;
import mekanism.common.lib.distribution.handler.SpecificAmountLongHandler;
import mekanism.common.lib.distribution.target.LongTarget;
import mekanism.common.util.EmitUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Distribution via EmitUtils (longs)")
class DistributionLongTest {

    static final Void VOID_RESOURCE = null;

    public static LongTarget getTargets(long infinite, long some, long none) {
        LongTarget target = new LongTarget();
        addTargets(target, InfiniteLongHandler::new, infinite);
        addTargets(target, PartialLongHandler::new, some);
        addTargets(target, () -> new SpecificAmountLongHandler(0), none);
        return target;
    }

    private static void addTargets(LongTarget targets, Supplier<LongHandler> targetSupplier, long count) {
        for (int i = 0; i < count; i++) {
            targets.addHandler(targetSupplier.get());
        }
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly")
    void testEvenDistribution() {
        long toSend = 10;
        LongTarget availableAcceptors = getTargets(toSend, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        for (LongHandler handler : availableAcceptors.handlers) {
            Assertions.assertEquals(1, handler.getAccepted());
        }
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly with more than 1 each")
    void testEvenDistribution2() {
        long toSend = 40;
        LongTarget availableAcceptors = getTargets(toSend / 4, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        for (LongHandler handler : availableAcceptors.handlers) {
            Assertions.assertEquals(4, handler.getAccepted());
        }
    }

    @Test
    @DisplayName("Test sending to non divisible amounts")
    void testRemainderDistribution() {
        long toSend = 10;
        LongTarget availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        int singleAccepted = 0, twoAccepted = 0;
        for (LongHandler handler : availableAcceptors.handlers) {
            Assertions.assertTrue(handler.getAccepted() == 1 || handler.getAccepted() == 2);
            if (handler.getAccepted() == 1) {
                singleAccepted++;
            } else {
                twoAccepted++;
            }
        }
        Assertions.assertEquals(4, singleAccepted);
        Assertions.assertEquals(3, twoAccepted);
    }

    @Test
    @DisplayName("Test sending to more targets than we have enough to send one to each of")
    void testAllRemainder() {
        long toSend = 3;
        LongTarget availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        int destinationsAccepted = 0;
        int destinationsNotAccepted = 0;
        for (LongHandler availableAcceptor : availableAcceptors.handlers) {
            if (availableAcceptor.getAccepted() > 0) {
                destinationsAccepted++;
                Assertions.assertEquals(1, availableAcceptor.getAccepted());
            } else {
                destinationsNotAccepted++;
            }
        }
        Assertions.assertEquals(3, destinationsAccepted);
        Assertions.assertEquals(4, destinationsNotAccepted);
    }

    @Test
    @DisplayName("Test to check if the remainder is being calculated correctly")
    void testCorrectRemainder() {
        long toSend = 5;
        //Three targets so that we have a split of one and a remainder of two (initial)
        //First one can accept exactly one
        //total to send -> 4, to split among -> 2, to send -> 2 (remainder none)
        LongTarget availableAcceptors = new LongTarget();
        availableAcceptors.addHandler(new SpecificAmountLongHandler(1));
        addTargets(availableAcceptors, () -> new SpecificAmountLongHandler(3), 2);
        long sent = EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE);
        if (sent > toSend) {
            Assertions.fail(String.format(Locale.ROOT, "expected: <%s> to be greater or equal to: <%s>", toSend, sent));
        }
    }

    @Test
    @DisplayName("Test to check if the remainder is able to be sent when having to fall back")
    void testCorrectFallbackRemainder() {
        long toSend = 9;
        LongTarget availableAcceptors = new LongTarget();
        LongHandler specificHandler = new SpecificAmountLongHandler(8);
        LongHandler lyingHandler = new LyingAmountLongHandler(1, 10);
        availableAcceptors.addHandler(specificHandler);
        availableAcceptors.addHandler(lyingHandler);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        Assertions.assertEquals(1, lyingHandler.getAccepted());
        Assertions.assertEquals(8, specificHandler.getAccepted());
    }

    @Test
    @DisplayName("Test to check if the remainder is able to be sent when having to fall back using the reversed order for the handlers")
    void testCorrectFallbackRemainderAltOrder() {
        long toSend = 9;
        LongTarget availableAcceptors = new LongTarget();
        LongHandler specificHandler = new SpecificAmountLongHandler(8);
        LongHandler lyingHandler = new LyingAmountLongHandler(1, 10);
        availableAcceptors.addHandler(lyingHandler);
        availableAcceptors.addHandler(specificHandler);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        Assertions.assertEquals(1, lyingHandler.getAccepted());
        Assertions.assertEquals(8, specificHandler.getAccepted());
    }
}