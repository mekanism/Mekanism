package mekanism.common.lib.distribution;

import mekanism.common.lib.distribution.target.IntegerTarget;
import mekanism.common.util.EmitUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.QuickTheory;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;
import org.quicktheories.dsl.TheoryBuilder2;
import org.quicktheories.impl.Constraint;

@DisplayName("Property based testing of distribution via EmitUtils")
class DistributionPropertyTest implements WithQuickTheories {

    private Gen<IntegerTarget> createTargets(int minInfinite, int maxInfinite, int minSome, int maxSome, int minNone, int maxNone) {
        Constraint infiniteConstraint = Constraint.between(minInfinite, maxInfinite).withShrinkPoint(0);
        Constraint someConstraint = Constraint.between(minSome, maxSome).withShrinkPoint(0);
        Constraint noneConstraint = Constraint.between(minNone, maxNone).withShrinkPoint(0);
        //Given random generator create integer target using the three constraints we defined above
        return prng -> DistributionTest.getTargets((int) prng.next(infiniteConstraint), (int) prng.next(someConstraint), (int) prng.next(noneConstraint));
    }

    private TheoryBuilder2<IntegerTarget, Integer> distributionTheory(int minInfinite, int maxInfinite, int minSome, int maxSome, int minNone, int maxNone) {
        return qt().forAll(createTargets(minInfinite, maxInfinite, minSome, maxSome, minNone, maxNone), integers().allPositive());
    }

    @Override
    public QuickTheory qt() {
        //Force our example count to be higher than the default by 100x
        return WithQuickTheories.super.qt().withExamples(100_000);
    }

    @Test
    @DisplayName("Test distribution")
    void testDistribution() {
        distributionTheory(0, 100, 0, 100, 0, 100).check((availableAcceptors, toSend) ->
              EmitUtils.sendToAcceptors(availableAcceptors, toSend, toSend) <= toSend
        );
    }

    @Test
    @DisplayName("Test distribution no partial")
    void testDistributionNoPartial() {
        distributionTheory(0, 100, 0, 0, 0, 100).check((availableAcceptors, toSend) ->
              EmitUtils.sendToAcceptors(availableAcceptors, toSend, toSend) <= toSend
        );
    }

    @Test
    @DisplayName("Test distribution no infinite")
    void testDistributionNoInfinite() {
        distributionTheory(0, 0, 0, 100, 0, 100).check((availableAcceptors, toSend) ->
              EmitUtils.sendToAcceptors(availableAcceptors, toSend, toSend) <= toSend
        );
    }
}