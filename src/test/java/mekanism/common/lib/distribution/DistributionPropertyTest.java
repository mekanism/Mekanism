package mekanism.common.lib.distribution;

import com.mojang.datafixers.util.Pair;
import java.util.Set;
import mekanism.common.util.EmitUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.api.Subject1;

@DisplayName("Property based testing of distribution via EmitUtils")
class DistributionPropertyTest implements WithQuickTheories {

    private Subject1<Pair<Set<Target<Integer, Integer, Integer>>, Integer>> distributionTheory(int minInfinite, int maxInfinite,
          int minSome, int maxSome, int minNone, int maxNone) {
        return qt().forAll(
              integers().between(minInfinite, maxInfinite),
              integers().between(minSome, maxSome),
              integers().between(minNone, maxNone),
              integers().allPositive()
        ).as((infinite, some, none, toSend) -> Pair.of(DistributionTest.getTargets(infinite, some, none), toSend));
    }

    private Subject1<Pair<Set<Target<Integer, Integer, Integer>>, Integer>> distributionTheory(int min, int max) {
        return distributionTheory(min, max, min, max, min, max);
    }

    @Test
    @DisplayName("Test distribution")
    void testDistribution() {
        distributionTheory(0, 100).check(val -> {
            Set<Target<Integer, Integer, Integer>> availableAcceptors = val.getFirst();
            int toSend = val.getSecond();
            return EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend) <= toSend;
        });
    }

    @Test
    @DisplayName("Test distribution no partial")
    void testDistributionNoPartial() {
        distributionTheory(0, 100, 0, 0, 0, 100).check(val -> {
            Set<Target<Integer, Integer, Integer>> availableAcceptors = val.getFirst();
            int toSend = val.getSecond();
            return EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend) <= toSend;
        });
    }

    @Test
    @DisplayName("Test distribution no infinite")
    void testDistributionNoInfinite() {
        distributionTheory(0, 0, 0, 100, 0, 100).check(val -> {
            Set<Target<Integer, Integer, Integer>> availableAcceptors = val.getFirst();
            int toSend = val.getSecond();
            return EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend) <= toSend;
        });
    }
}