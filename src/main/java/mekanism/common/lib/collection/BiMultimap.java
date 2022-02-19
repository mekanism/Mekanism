package mekanism.common.lib.collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public class BiMultimap<K, V> {

    private final SetMultimap<K, V> map = HashMultimap.create();
    private final SetMultimap<V, K> reverseMap = HashMultimap.create();

    public boolean put(K key, V value) {
        return map.put(key, value) && reverseMap.put(value, key);
    }

    public boolean putAll(Collection<K> keys, V value) {
        boolean changed = false;
        for (K key : keys) {
            changed |= put(key, value);
        }
        return changed;
    }

    public boolean remove(K key, V value) {
        return map.remove(key, value) && reverseMap.remove(value, key);
    }

    public boolean removeKey(K key) {
        boolean changed = false;
        for (V value : getValues(key)) {
            changed |= reverseMap.remove(value, key);
        }
        map.removeAll(key);
        return changed;
    }

    public boolean removeValue(V value) {
        boolean changed = false;
        for (K key : getKeys(value)) {
            changed |= map.remove(key, value);
        }
        reverseMap.removeAll(value);
        return changed;
    }

    public Set<K> getAllKeys() {
        return map.keySet();
    }

    public Set<V> getValues(K key) {
        return map.get(key);
    }

    public Set<K> getKeys(V value) {
        return reverseMap.get(value);
    }

    public Set<Entry<K, V>> getEntries() {
        return map.entries();
    }

    public Set<Entry<V, K>> getReverseEntries() {
        return reverseMap.entries();
    }

    public boolean hasAllKeys(Collection<K> keys) {
        return getAllKeys().containsAll(keys);
    }

    public void clear() {
        map.clear();
        reverseMap.clear();
    }
}