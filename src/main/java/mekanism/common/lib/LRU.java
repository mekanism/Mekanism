package mekanism.common.lib;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class LRU<T> extends AbstractCollection<T> {

    private final Map<T, LRUEntry> lookupMap = new Object2ObjectOpenHashMap<>();

    private final LRUEntry head, tail;
    private int size;

    public LRU() {
        head = new LRUEntry(null);
        tail = new LRUEntry(null);
        head.next = tail;
        tail.prev = head;
    }

    private void remove(LRUEntry entry) {
        entry.next.prev = entry.prev;
        entry.prev.next = entry.next;
        size--;
        lookupMap.remove(entry.value);
    }

    private void addFirst(LRUEntry entry) {
        entry.prev = head;
        entry.next = head.next;
        entry.next.prev = entry;
        head.next = entry;
        size++;
        lookupMap.put(entry.value, entry);
    }

    @Override
    public boolean add(T element) {
        addFirst(new LRUEntry(element));
        return true;
    }

    public void moveUp(T element) {
        LRUEntry entry = lookupMap.get(element);
        if (entry == null) {
            return;
        }
        remove(entry);
        addFirst(entry);
    }

    @Override
    public boolean remove(Object element) {
        LRUEntry entry = lookupMap.get(element);
        if (entry == null) {
            return false;
        }
        remove(entry);
        return true;
    }

    @Override
    public boolean contains(Object element) {
        return lookupMap.containsKey(element);
    }

    @Override
    public int size() {
        return size;
    }

    public void reverseIterate(Consumer<T> callback) {
        LRUEntry ptr = tail.prev;
        while (ptr != head) {
            callback.accept(ptr.value);
            ptr = ptr.prev;
        }
    }

    private class LRUEntry {

        private final T value;
        private LRUEntry prev, next;

        private LRUEntry(T value) {
            this.value = value;
        }
    }

    @Nonnull
    @Override
    public LRUIterator iterator() {
        return new LRUIterator();
    }

    public LRUIterator descendingIterator() {
        return new LRUIterator().reverse();
    }

    public class LRUIterator implements Iterator<T> {

        boolean reverse = false;
        LRUEntry curEntry = head;

        @Override
        public boolean hasNext() {
            return reverse ? curEntry.prev != head : curEntry.next != tail;
        }

        @Override
        public T next() {
            if (reverse) {
                curEntry = curEntry.prev;
                if (curEntry == head) {
                    throw new NoSuchElementException("Reached beginning of LRU");
                }
            } else {
                curEntry = curEntry.next;
                if (curEntry == tail) {
                    throw new NoSuchElementException("Reached end of LRU");
                }
            }
            return curEntry != null ? curEntry.value : null;
        }

        public LRUIterator reverse() {
            reverse = true;
            curEntry = tail;
            return this;
        }
    }
}
