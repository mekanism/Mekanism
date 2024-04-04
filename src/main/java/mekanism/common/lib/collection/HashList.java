package mekanism.common.lib.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Copied from org.antlr.v4.runtime.misc.OrderedHashSet. A HashMap that remembers the order that the elements were added. You can alter the ith element with set(i,value)
 * too :) Unique list.
 *
 * @param <T> element type
 */
public class HashList<T> extends LinkedHashSet<T> {

    @NotNull
    protected List<T> list;

    public HashList() {
        list = new ArrayList<>();
    }

    public HashList(Collection<? extends T> toCopy) {
        this();
        addAll(toCopy);
    }

    public HashList(int initialCapacity) {
        super(initialCapacity);
        list = new ArrayList<>(initialCapacity);
    }

    /**
     * Replace an existing value with a new value; updates the element list and the hash table.
     */
    public T set(int i, T value) {
        T oldElement = list.get(i);
        list.set(i, value); // update list
        super.remove(oldElement); // now update the set: remove/add
        super.add(value);
        return oldElement;
    }

    /**
     * Add a value to list; keep in hashtable for consistency also; Key is object itself.  Good for say asking if a certain string is in a list of strings.
     */
    @Override
    public boolean add(T value) {
        boolean result = super.add(value);
        if (result) {  // only track if new element not in set
            list.add(value);
        }
        return result;
    }

    public void add(int i, T value) {
        boolean result = super.add(value);
        if (result) {  // only track if new element not in set
            list.add(i, value);
        }
    }

    @Override
    public void clear() {
        list.clear();
        super.clear();
    }

    public T get(int i) {
        return list.get(i);
    }

    @Nullable
    public T getOrNull(int index) {
        return index >= 0 && index < size() ? get(index) : null;
    }

    /**
     * Return the List holding list of table elements.  Note that you are NOT getting a copy so don't write to the list.
     */
    public List<T> elements() {
        return list;
    }

    @Override
    public Object clone() {
        @SuppressWarnings("unchecked") // safe (result of clone)
        HashList<T> dup = (HashList<T>) super.clone();
        dup.list = new ArrayList<>(this.list);
        return dup;
    }

    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public boolean replace(T existing, @Nullable T replacement) {
        if (existing.equals(replacement)) {
            //If the two elements are the same don't do anything
            return false;
        }
        int index = indexOf(existing);
        if (index != -1) {
            if (replacement == null || contains(replacement)) {
                //If we have no replacement or the list already contains the element we want to replace it with
                // just remove the existing element
                remove(index);
            } else {
                //Just directly replace the element with the new version
                set(index, replacement);
            }
            return true;
        }
        return false;
    }

    public T remove(int i) {
        T o = list.remove(i);
        super.remove(o);
        return o;
    }

    @Override
    public boolean remove(Object o) {
        list.remove(o);
        return super.remove(o);
    }

    public int indexOf(T obj) {
        return contains(obj) ? list.indexOf(obj) : -1;
    }

    public void swap(int source, int target, BiConsumer<T, T> postSwap) {
        // Make sure both source and target are legal values
        if (source == target || source < 0 || target < 0) {
            return;
        }
        int size = size();
        if (source >= size || target >= size) {
            return;
        }
        // Perform swap
        T sourceT = list.get(source);
        T targetT = list.get(target);
        list.set(source, targetT);
        list.set(target, sourceT);
        postSwap.accept(sourceT, targetT);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HashList<?>)) {
            return o instanceof List && list.equals(o);
        }
        return list.equals(((HashList<?>) o).list);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }
}