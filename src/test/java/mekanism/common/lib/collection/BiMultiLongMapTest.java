package mekanism.common.lib.collection;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BiMultiLongMapTest {
    @Test
    void testSingleValue() {
        BiMultiLongmap<String> map = new BiMultiLongmap<>();
        String first = "first";
        map.put(1, first);
        map.putAll(new long[]{2, 3}, first);

        for (long i = 1; i < 4; i++) {
            Assertions.assertIterableEquals(Collections.singleton(first), map.getValues(i));
        }

        Assertions.assertIterableEquals(Arrays.asList(1L, 2L, 3L), map.getKeys(first));

        map.removeKey(3);
        Assertions.assertIterableEquals(Arrays.asList(1L, 2L), map.getKeys(first));
        Assertions.assertNull(map.getValues(3));

        map.removeValue(first);
        Assertions.assertTrue(map.getAllKeys().isEmpty());
    }
}
