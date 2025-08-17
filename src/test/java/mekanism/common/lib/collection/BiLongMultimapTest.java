package mekanism.common.lib.collection;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BiLongMultimapTest {
    @Test
    @DisplayName("Test BiLongMultimaps with a single value")
    void testSingleValue() {
        BiLongMultimap<String> map = new BiLongMultimap<>();
        String first = "first";
        map.put(1, first);
        map.putAll(new long[]{2, 3}, first);

        Assertions.assertIterableEquals(List.of(1L, 2L, 3L), map.getAllKeys());

        for (long i = 1; i < 4; i++) {
            Assertions.assertIterableEquals(Collections.singleton(first), map.getValues(i));
        }

        Assertions.assertIterableEquals(List.of(1L, 2L, 3L), map.getKeys(first));

        map.removeKey(3);
        Assertions.assertIterableEquals(List.of(1L, 2L), map.getAllKeys());
        Assertions.assertIterableEquals(List.of(1L, 2L), map.getKeys(first));
        Assertions.assertNull(map.getValues(3));

        map.removeValue(first);
        Assertions.assertTrue(map.getAllKeys().isEmpty());
    }

    @Test
    @DisplayName("Test BiLongMultimaps with multiple values")
    void testMultipleValues() {
        BiLongMultimap<String> map = new BiLongMultimap<>();
        String first = "first";
        String second = "second";
        String third = "third";
        map.putAll(new long[]{1, 2}, first);
        map.putAll(new long[]{2, 3}, second);
        map.putAll(new long[]{3, 4}, third);

        Assertions.assertIterableEquals(List.of(1L, 2L, 3L, 4L), map.getAllKeys());
        Assertions.assertIterableEquals(List.of(first), map.getValues(1));
        Assertions.assertIterableEquals(List.of(first, second), map.getValues(2).stream().sorted().toList());
        Assertions.assertIterableEquals(List.of(second, third), map.getValues(3).stream().sorted().toList());
        Assertions.assertIterableEquals(List.of(third), map.getValues(4));
        Assertions.assertIterableEquals(List.of(1L, 2L), map.getKeys(first));
        Assertions.assertIterableEquals(List.of(2L, 3L), map.getKeys(second));
        Assertions.assertIterableEquals(List.of(3L, 4L), map.getKeys(third));

        map.remove(1, third);
        Assertions.assertIterableEquals(List.of(1L, 2L, 3L, 4L), map.getAllKeys(), "no change expected");

        map.remove(4, third);
        Assertions.assertIterableEquals(List.of(1L, 2L, 3L), map.getAllKeys(), "expected 4 gone");
        Assertions.assertIterableEquals(List.of(3L), map.getKeys(third));
        Assertions.assertNull(map.getValues(4));

        map.removeValue(second);
        Assertions.assertIterableEquals(List.of(1L, 2L, 3L), map.getAllKeys(), "same keys");
        Assertions.assertIterableEquals(List.of(first), map.getValues(2).stream().sorted().toList());
        Assertions.assertIterableEquals(List.of(third), map.getValues(3).stream().sorted().toList());
        Assertions.assertNull(map.getKeys(second));

        map.remove(3, third);
        Assertions.assertIterableEquals(List.of(1L, 2L), map.getAllKeys(), "expected 3 gone");
    }
}
