package mekanism.common.lib.distribution;

import mekanism.common.lib.distribution.target.IntegerTarget;
import mekanism.common.util.EmitUtils;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.Assertions;

@Label("Property based testing of distribution via EmitUtils")
class DistributionPropertyTest {

    static final Void VOID_RESOURCE = null;

    //Force our example count to be higher than the default by 100x
    private static final int TRIES = 100_000;

    @Property(tries = TRIES)
    @Label("Test distribution")
    void testDistribution(@ForAll @IntRange(max = 100) int infinite, @ForAll @IntRange(max = 100) int some, @ForAll @IntRange(max = 100) int none,
          @ForAll @Positive int toSend) {
        IntegerTarget availableAcceptors = DistributionTest.getTargets(infinite, some, none);
        Assertions.assertTrue(EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE) <= toSend);
    }

    @Property(tries = TRIES)
    @Label("Test distribution no partial")
    void testDistributionNoPartial(@ForAll @IntRange(max = 100) int infinite, @ForAll @IntRange(max = 100) int none, @ForAll @Positive int toSend) {
        IntegerTarget availableAcceptors = DistributionTest.getTargets(infinite, 0, none);
        Assertions.assertTrue(EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE) <= toSend);
    }

    @Property(tries = TRIES)
    @Label("Test distribution no infinite")
    void testDistributionNoInfinite(@ForAll @IntRange(max = 100) int some, @ForAll @IntRange(max = 100) int none, @ForAll @Positive int toSend) {
        IntegerTarget availableAcceptors = DistributionTest.getTargets(0, some, none);
        Assertions.assertTrue(EmitUtils.sendToAcceptors(availableAcceptors, toSend, VOID_RESOURCE) <= toSend);
    }
}