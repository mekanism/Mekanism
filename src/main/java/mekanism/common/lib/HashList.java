package mekanism.common.lib;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HashList<T> extends AbstractList<T> {

    private final List<T> list;

    public HashList(List<T> newList) {
        list = newList;
    }

    public HashList() {
        this(256);
    }

    public HashList(int initialCapacity) {
        list = new ArrayList<>(initialCapacity);
    }

    @Override
    public boolean contains(Object obj) {
        return list.contains(obj);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Nullable
    public T getOrNull(int index) {
        return index >= 0 && index < size() ? get(index) : null;
    }

    @Override
    public boolean add(T obj) {
        return !list.contains(obj) && list.add(obj);
    }

    @Override
    public void add(int index, T obj) {
        if (!list.contains(obj)) {
            if (index > size()) {
                for (int i = size(); i <= index - 1; i++) {
                    list.add(i, null);
                }
            }
            list.add(index, obj);
        }
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    public void replace(int index, T obj) {
        if (getOrNull(index) != null) {
            remove(index);
        }
        add(index, obj);
    }

    @Override
    public boolean remove(Object obj) {
        return list.remove(obj);
    }

    @Override
    public int indexOf(Object obj) {
        return list.indexOf(obj);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public HashList<T> clone() {
        return new HashList<>(new ArrayList<>(list));
    }

    public void swap(int source, int target) {
        // Make sure both source and target are legal values
        if (source == target || source < 0 || target < 0) {
            return;
        }
        int size = size();
        if (source >= size || target >= size) {
            return;
        }
        // Perform swap
        T temp = list.get(source);
        list.set(source, list.get(target));
        list.set(target, temp);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof List && list.equals(obj);
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}