package mekanism.common.distribution;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import mekanism.common.distribution.target.InfiniteIntegerTarget;
import mekanism.common.util.EmitUtils;
import net.minecraft.util.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test Distribution via EmitUtils")
class DistributionTest {

    //TODO: Add more tests to this class, some of which should potentially use property based testing with quick theories
    private static Set<InfiniteIntegerTarget> getTargets(int count) {
        Set<InfiniteIntegerTarget> targets = new ObjectOpenHashSet<>();
        for (int i = 0; i < count; i++) {
            InfiniteIntegerTarget target = new InfiniteIntegerTarget();
            target.addHandler(Direction.NORTH, i);
            targets.add(target);
        }
        return targets;
    }

    @Test
    @DisplayName("Test sending to targets where the amounts divide evenly")
    void testEvenDistribution() {
        int toSend = 10;
        Set<InfiniteIntegerTarget> availableAcceptors = getTargets(toSend);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
    }

    @Test
    @DisplayName("Test sending to non divisible amounts")
    void testRemainderDistribution() {
        int toSend = 10;
        Set<InfiniteIntegerTarget> availableAcceptors = getTargets(7);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
    }

    @Test
    @DisplayName("Test sending to more targets than we have enough to send one to each of")
    void testAllRemainder() {
        int toSend = 3;
        Set<InfiniteIntegerTarget> availableAcceptors = getTargets(7);
        Assertions.assertEquals(toSend, EmitUtils.sendToAcceptors(availableAcceptors, availableAcceptors.size(), toSend, toSend));
    }
}