package mekanism.common.lib.collection;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IndexedCuboidMapTest {

    static final String value1 = "test";
    static final String value2 = "foo";
    static final BlockPos center1 = new BlockPos(8, 8, 8);
    static final BlockPos center2 = new BlockPos(16 + 8, 8, 8);
    static final int radiusSmall = 4;
    static final int radiusLarge = 16;

    @Test
    void testSingleValueSingleChunk() {
        singleValue(radiusSmall);
    }

    @Test
    void testSingleValueSingleChunkRemoveCenter() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.removeAt(center1);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueSingleChunkRemoveValue() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.remove(value1);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueSingleChunkRemoveIf() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.removeIf(value1::equals);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueSingleChunkClear() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.clear();

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }
    
    @Test
    void testSingleValueMultiChunk() {
        singleValue(radiusLarge);
    }

    @Test
    void testSingleValueMultiChunkRemoveCenter() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.removeAt(center1);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueMultiChunkRemoveValue() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.remove(value1);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueMultiChunkRemoveIf() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.removeIf(value1::equals);

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testSingleValueMultiChunkClear() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = singleValue(myRadius);

        map.clear();

        assertNotPresent(map, myRadius, center1, value1);
        assertEmpty(map);
    }

    @Test
    void testDualValueSingleChunk() {
        dualValue(radiusSmall);
    }

    @Test
    void testDualValueSingleChunkRemoveCenter() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.removeAt(center1);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.removeAt(center2);

        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueSingleChunkRemoveValue() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.remove(value1);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.remove(value2);

        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueSingleChunkRemoveIf() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.removeIf(value1::equals);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.removeIf(v->v.equals(value2));

        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueSingleChunkClear() {
        int myRadius = radiusSmall;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.clear();

        assertNotPresent(map, myRadius, center1, value1);
        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }
    
    @Test
    void testDualValueMultiChunk() {
        dualValue(radiusLarge);
    }

    @Test
    void testDualValueMultiChunkRemoveCenter() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.removeAt(center1);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.removeAt(center2);

        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueMultiChunkRemoveValue() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.remove(value1);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.remove(value2);

        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueMultiChunkRemoveIf() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.removeIf(value1::equals);

        assertNotPresent(map, myRadius, center1, value1);
        assertPresent(center2, myRadius, value2, map);

        map.removeIf(value2::equals);
        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    @Test
    void testDualValueMultiChunkClear() {
        int myRadius = radiusLarge;
        IndexedCuboidMap<String> map = dualValue(myRadius);

        map.clear();

        assertNotPresent(map, myRadius, center1, value1);
        assertNotPresent(map, myRadius, center2, value2);
        assertEmpty(map);
    }

    private static IndexedCuboidMap<String> singleValue(int radius) {
        IndexedCuboidMap<String> map = new IndexedCuboidMap<>();
        map.track(value1, center1, radius);

        assertPresent(center1, radius, value1, map);
        List<String> singletonValue = List.of(value1);
        Assertions.assertIterableEquals(singletonValue, new AsIterable<>(map.find(center1)));
        Assertions.assertIterableEquals(singletonValue, new AsIterable<>(map.allCenteredInChunk(ChunkPos.asLong(center1))));

        return map;
    }

    private static IndexedCuboidMap<String> dualValue(int radius) {
        IndexedCuboidMap<String> map = new IndexedCuboidMap<>();
        map.track(value1, center1, radius);
        map.track(value2, center2, radius);

        assertPresent(center1, radius, value1, map);
        assertPresent(center2, radius, value2, map);

        return map;
    }

    private static void assertPresent(BlockPos centre, int radius, String value, IndexedCuboidMap<String> map) {
        Assertions.assertFalse(map.isEmpty());
        Assertions.assertFalse(map.indexIsEmpty());
        Assertions.assertTrue(map.values().contains(value));
        Assertions.assertEquals(value, map.findFirstAt(centre), "expected value we added");

        Assertions.assertTrue(contains(map.find(centre), value));
        Assertions.assertTrue(contains(map.allCenteredInChunk(ChunkPos.asLong(centre)), value));

        for (BlockPos checkPos : BlockPos.betweenClosed(centre.offset(-radius, -radius, -radius), centre.offset(radius, radius, radius))) {
            Assertions.assertTrue(contains(map.find(checkPos), value), "expected to find value in search grid");
        }

        Assertions.assertFalse(contains(map.find(centre.offset(radius + 1, 0, 0)), value), "position outside should not contain value");
    }

    private static void assertNotPresent(IndexedCuboidMap<String> map, int myRadius, BlockPos centre, String value) {
        Assertions.assertNotEquals(value, map.findFirstAt(centre), "expected no match");

        Assertions.assertFalse(contains(map.find(centre), value));
        Assertions.assertFalse(contains(map.allCenteredInChunk(ChunkPos.asLong(centre)), value));

        for (BlockPos checkPos : BlockPos.betweenClosed(centre.offset(-myRadius, -myRadius, -myRadius), centre.offset(myRadius, myRadius, myRadius))) {
            Assertions.assertFalse(contains(map.find(checkPos), value), "expected to not find value in search grid");
        }

        Assertions.assertFalse(contains(map.find(centre.offset(myRadius + 1, 0, 0)), value), "position outside should not contain value");
    }

    private static void assertEmpty(IndexedCuboidMap<String> map) {
        Assertions.assertTrue(map.isEmpty());
        Assertions.assertTrue(map.indexIsEmpty());
        Assertions.assertTrue(map.values().isEmpty());
    }

    private static boolean contains(Iterator<String> iterator, String value) {
        while (iterator.hasNext()) {
            if (value.equals(iterator.next())) {
                return true;
            }
        }
        return false;
    }

    private record AsIterable<T>(Iterator<T> iterator) implements Iterable<T> {}
}
