package mekanism.common.lib.distribution;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import mekanism.common.lib.distribution.target.InfiniteIntegerTarget;
import mekanism.common.lib.distribution.target.IntegerTarget;
import mekanism.common.lib.distribution.target.PartialIntegerTarget;
import mekanism.common.lib.distribution.target.SpecificAmountIntegerTarget;
import mekanism.common.util.EmitUtils;
import net.minecraft.util.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Distribution via EmitUtils")
class DistributionTest {

    public static Set<IntegerTarget> getTargets(int infinite, int some, int none) {
        Direction side = Direction.NORTH;
        Set<IntegerTarget> targets = new ObjectOpenHashSet<>();
        int index = addTargets(targets, InfiniteIntegerTarget::new, infinite, side, 0);
        index = addTargets(targets, PartialIntegerTarget::new, some, side, index);
        addTargets(targets, () -> new SpecificAmountIntegerTarget(0), none, side, index);
        return targets;
    }

    private static int addTargets(Set<IntegerTarget> targets, Supplier<IntegerTarget> targetSupplier, int count,
          Direction side, int index) {
        for (int i = 0; i < count; i++) {
            IntegerTarget target = targetSupplier.get();
            target.addHandler(side, index + i);
            targets.add(target);
        }
        return index + count;
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly")
    void testEvenDistribution() {
        int toSend = 10;
        Set<IntegerTarget> availableAcceptors = getTargets(toSend, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
    }

    @Test
    @DisplayName("Test sending to non divisible amounts")
    void testRemainderDistribution() {
        int toSend = 10;
        Set<IntegerTarget> availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
    }

    @Test
    @DisplayName("Test sending to more targets than we have enough to send one to each of")
    void testAllRemainder() {
        int toSend = 3;
        Set<IntegerTarget> availableAcceptors = getTargets(7, 0, 0);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
        //TODO: Make it so that we try to send this more evenly initially before falling back to send to remainders?
        /*for (IntegerTarget availableAcceptor : availableAcceptors) {
            System.out.println("Amount accepted: " + availableAcceptor.getAccepted());
        }*/
    }

    @Test
    @DisplayName("Test to check if the remainder is being calculated correctly")
    void testCorrectRemainder() {
        int toSend = 5;
        //Three targets so that we have a split of one and a remainder of two (initial)
        //First one can accept exactly one
        //total to send -> 4, to split among -> 2, to send -> 2 (remainder none)
        Direction side = Direction.NORTH;
        Set<IntegerTarget> availableAcceptors = new ObjectOpenHashSet<>();
        IntegerTarget target = new SpecificAmountIntegerTarget(1);
        target.addHandler(side, 0);
        availableAcceptors.add(target);
        addTargets(availableAcceptors, () -> new SpecificAmountIntegerTarget(3), 2, side, 1);
        int sent = EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend);
        if (sent > toSend) {
            Assertions.fail(String.format("expected: <%s> to be greater or equal to: <%s>", toSend, sent));
        }
    }
}