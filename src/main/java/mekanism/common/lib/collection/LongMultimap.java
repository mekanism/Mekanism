package mekanism.common.lib.collection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.ArrayList;
import java.util.List;

/// Like a Multimap but with long as key, using FastUtils
///
/// @param <V> the value type
public class LongMultimap<V> {

    private final Long2ObjectSortedMap<List<V>> map = new Long2ObjectAVLTreeMap<>();

    public boolean put(long key, V value) {
        return map.computeIfAbsent(key, k->new ArrayList<>()).add(value);
    }

    @CanIgnoreReturnValue
    public boolean remove(long key, V value) {
        List<V> values = map.get(key);
        boolean removed = false;
        if (values != null) {
            removed = values.remove(value);
            if (values.isEmpty()) {
                map.remove(key);
            }
        }

        return removed;
    }

    @CanIgnoreReturnValue
    public boolean removeAll(long key) {
        return map.remove(key) != null;
    }

    public LongSortedSet getAllKeys() {
        return map.keySet();
    }

    public List<V> get(long key) {
        return map.get(key);
    }

    public boolean hasAllKeys(LongCollection keys) {
        return getAllKeys().containsAll(keys);
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(long key) {
        return map.containsKey(key);
    }
}