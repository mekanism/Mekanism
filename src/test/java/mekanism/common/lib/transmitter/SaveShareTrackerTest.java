package mekanism.common.lib.transmitter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test save share tracking")
class SaveShareTrackerTest {

    @Test
    @DisplayName("Test shares need calculating initially, even at game time zero")
    void testInitial() {
        Assertions.assertTrue(new SaveShareTracker().needsRecalculation(0, 0));
    }

    @Test
    @DisplayName("Test shares are reused within a tick when the buffer is unchanged")
    void testReuse() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        Assertions.assertFalse(tracker.needsRecalculation(10, 500));
    }

    @Test
    @DisplayName("Test shares are recalculated on a new tick")
    void testNewTick() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        Assertions.assertTrue(tracker.needsRecalculation(11, 500));
    }

    @Test
    @DisplayName("Test shares are recalculated when the buffer drops within a tick")
    void testBufferDecrease() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        Assertions.assertTrue(tracker.needsRecalculation(10, 400));
    }

    @Test
    @DisplayName("Test shares are recalculated when the buffer grows within a tick")
    void testBufferIncrease() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        Assertions.assertTrue(tracker.needsRecalculation(10, 600));
    }

    @Test
    @DisplayName("Test taking shares does not count as an outside change")
    void testSharesTaken() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        tracker.shareTaken(200);
        Assertions.assertFalse(tracker.needsRecalculation(10, 300));
        tracker.shareTaken(300);
        Assertions.assertFalse(tracker.needsRecalculation(10, 0));
    }

    @Test
    @DisplayName("Test an outside change after taking a share is detected")
    void testOutsideChangeAfterTake() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        tracker.shareTaken(200);
        Assertions.assertTrue(tracker.needsRecalculation(10, 250));
    }

    @Test
    @DisplayName("Test recalculating resets the expected amount")
    void testRecalculatedResets() {
        SaveShareTracker tracker = new SaveShareTracker();
        tracker.recalculated(10, 500);
        tracker.shareTaken(200);
        tracker.recalculated(10, 250);
        Assertions.assertFalse(tracker.needsRecalculation(10, 250));
    }
}
