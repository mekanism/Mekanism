package mekanism.common.lib.distribution;

import java.util.Locale;
import java.util.function.Supplier;
import mekanism.common.lib.distribution.handler.InfiniteIntegerHandler;
import mekanism.common.lib.distribution.handler.IntegerHandler;
import mekanism.common.lib.distribution.handler.LyingAmountIntegerHandler;
import mekanism.common.lib.distribution.handler.PartialIntegerHandler;
import mekanism.common.lib.distribution.handler.SpecificAmountIntegerHandler;
import mekanism.common.lib.distribution.target.IntegerTarget;
import mekanism.common.util.EmitUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Distribution via EmitUtils")
class DistributionTest {

    static final Void VOID_RESOURCE = null;

    public static IntegerTarget getTargets(int infinite, int some, int none) {
        IntegerTarget target = new IntegerTarget();
        addTargets(target, InfiniteIntegerHandler::new, infinite);
        addTargets(target, PartialIntegerHandler::new, some);
        addTargets(target, () -> new SpecificAmountIntegerHandler(0), none);
        return target;
    }

    private static void addTargets(IntegerTarget targets, Supplier<IntegerHandler> targetSupplier, int count) {
        for (int i = 0; i < count; i++) {
            targets.addHandler(targetSupplier.get());
        }
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly")
    void testEvenDistribution() {
        int toSend = 10;
        IntegerTarget availableAcceptors = getTargets(toSend, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        for (IntegerHandler handler : availableAcceptors.handlers) {
            Assertions.assertEquals(1, handler.getAccepted());
        }
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly with more than 1 each")
    void testEvenDistribution2() {
        int toSend = 40;
        IntegerTarget availableAcceptors = getTargets(toSend / 4, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        for (IntegerHandler handler : availableAcceptors.handlers) {
            Assertions.assertEquals(4, handler.getAccepted());
        }
    }

    @Test
    @DisplayName("Test sending to non divisible amounts")
    void testRemainderDistribution() {
        int toSend = 10;
        IntegerTarget availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        int singleAccepted = 0, twoAccepted = 0;
        for (IntegerHandler handler : availableAcceptors.handlers) {
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
        int toSend = 3;
        IntegerTarget availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        int destinationsAccepted = 0;
        int destinationsNotAccepted = 0;
        for (IntegerHandler availableAcceptor : availableAcceptors.handlers) {
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
        int toSend = 5;
        //Three targets so that we have a split of one and a remainder of two (initial)
        //First one can accept exactly one
        //total to send -> 4, to split among -> 2, to send -> 2 (remainder none)
        IntegerTarget availableAcceptors = new IntegerTarget();
        availableAcceptors.addHandler(new SpecificAmountIntegerHandler(1));
        addTargets(availableAcceptors, () -> new SpecificAmountIntegerHandler(3), 2);
        int sent = EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE);
        if (sent > toSend) {
            Assertions.fail(String.format(Locale.ROOT, "expected: <%s> to be greater or equal to: <%s>", toSend, sent));
        }
    }

    @Test
    @DisplayName("Test to check if the remainder is able to be sent when having to fall back")
    void testCorrectFallbackRemainder() {
        int toSend = 9;
        IntegerTarget availableAcceptors = new IntegerTarget();
        IntegerHandler specificHandler = new SpecificAmountIntegerHandler(8);
        IntegerHandler lyingHandler = new LyingAmountIntegerHandler(1, 10);
        availableAcceptors.addHandler(specificHandler);
        availableAcceptors.addHandler(lyingHandler);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        Assertions.assertEquals(1, lyingHandler.getAccepted());
        Assertions.assertEquals(8, specificHandler.getAccepted());
    }

    @Test
    @DisplayName("Test to check if the remainder is able to be sent when having to fall back using the reversed order for the handlers")
    void testCorrectFallbackRemainderAltOrder() {
        int toSend = 9;
        IntegerTarget availableAcceptors = new IntegerTarget();
        IntegerHandler specificHandler = new SpecificAmountIntegerHandler(8);
        IntegerHandler lyingHandler = new LyingAmountIntegerHandler(1, 10);
        availableAcceptors.addHandler(lyingHandler);
        availableAcceptors.addHandler(specificHandler);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE));
        Assertions.assertEquals(1, lyingHandler.getAccepted());
        Assertions.assertEquals(8, specificHandler.getAccepted());
    }
}