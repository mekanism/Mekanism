package mekanism.common.lib.collection;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import org.jetbrains.annotations.Nullable;

//Copy of Collections#emptyMap as EMPTY_NAVIGABLE_MAP does not short circuit checks, and also does an unchecked cast in get to Comparable
@NothingNullByDefault
public class EmptySequencedMap<K, V> extends AbstractMap<K, V> implements SequencedMap<K, V> {

    private static final SequencedMap<?, ?> EMPTY_MAP = new EmptySequencedMap<>();

    @SuppressWarnings("unchecked")
    public static <K, V> SequencedMap<K, V> emptyMap() {
        return (SequencedMap<K, V>) EMPTY_MAP;
    }

    private EmptySequencedMap() {
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Nullable
    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.emptySet();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        return o instanceof SequencedMap<?,?> other && other.isEmpty();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    // Override default methods in Map
    @Override
    public V getOrDefault(Object k, V defaultValue) {
        return defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key,
          Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key,
          BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key,
          BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value,
          BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SequencedMap<K, V> reversed() {
        //Don't need to reverse it as it is empty
        return this;
    }

    // Override default methods in SequencedMap
    @Nullable
    @Override
    public Entry<K, V> firstEntry() {
        return null;
    }

    @Nullable
    @Override
    public Entry<K, V> lastEntry() {
        return null;
    }

    @Nullable
    @Override
    public Entry<K, V> pollFirstEntry() {
        return null;
    }

    @Nullable
    @Override
    public Entry<K, V> pollLastEntry() {
        return null;
    }

    @Override
    public SequencedSet<K> sequencedKeySet() {
        return Collections.emptySortedSet();
    }

    @Override
    public SequencedCollection<V> sequencedValues() {
        return Collections.emptySortedSet();
    }

    @Override
    public SequencedSet<Entry<K, V>> sequencedEntrySet() {
        return Collections.emptySortedSet();
    }
}