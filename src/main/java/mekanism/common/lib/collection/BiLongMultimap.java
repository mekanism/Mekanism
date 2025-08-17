package mekanism.common.lib.collection;

import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Like {@link BiMultimap} but with long as key, using FastUtils
 * @param <V> the value type
 */
public class BiLongMultimap<V> {

    private final Long2ObjectSortedMap<Set<V>> map = new Long2ObjectAVLTreeMap<>();
    private final Map<V, LongSortedSet> reverseMap = new HashMap<>();

    public boolean put(long key, V value) {
        boolean changed1 = map.computeIfAbsent(key, k->new HashSet<>()).add(value);
        return reverseMap.computeIfAbsent(value, k->new LongRBTreeSet()).add(key) || changed1;
    }

    public boolean putAll(LongCollection keys, V value) {
        boolean changed = false;
        LongIterator it = keys.iterator();
        while (it.hasNext()) {
            changed |= put(it.nextLong(), value);
        }
        return changed;
    }

    public boolean putAll(long[] keys, V value) {
        boolean changed = false;
        for (long key : keys) {
            changed |= put(key, value);
        }
        return changed;
    }

    public boolean remove(long key, V value) {
        Set<V> valueSet = map.get(key);
        boolean remove1 = false;
        if (valueSet != null) {
            remove1 = valueSet.remove(value);
            if (valueSet.isEmpty()) {
                map.remove(key);
            }
        }
        boolean remove2 = false;
        LongSet keyset = reverseMap.get(value);
        if (keyset != null) {
            remove2 = keyset.remove(key);
            if (keyset.isEmpty()) {
                reverseMap.remove(value);
            }
        }
        return remove1 && remove2;
    }

    public boolean removeKey(long key) {
        boolean changed = false;
        for (V value : new ArrayList<>(getValues(key))) {
            LongSet longs = reverseMap.get(value);
            if (longs != null) {
                changed |= longs.remove(key);
                if (longs.isEmpty()) {
                    reverseMap.remove(value);
                }
            }
        }
        map.remove(key);
        return changed;
    }

    public boolean removeValue(V value) {
        boolean changed = false;
        LongIterator iterator = getKeys(value).iterator();
        while (iterator.hasNext()) {
            long key = iterator.nextLong();
            Set<V> vs = map.get(key);
            if (vs != null) {
                changed |= vs.remove(value);
                if (vs.isEmpty()) {
                    map.remove(key);
                }
            }
        }
        reverseMap.remove(value);
        return changed;
    }

    public LongSortedSet getAllKeys() {
        return map.keySet();
    }

    public Set<V> getValues(long key) {
        return map.get(key);
    }

    public LongSortedSet getKeys(V value) {
        return reverseMap.get(value);
    }

    public boolean hasAllKeys(LongCollection keys) {
        return getAllKeys().containsAll(keys);
    }

    public void clear() {
        map.clear();
        reverseMap.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty() && reverseMap.isEmpty();
    }
}