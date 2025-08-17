package mekanism.common.lib.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FilterTransformIterator<IN, OUT> implements Iterator<OUT> {

    private final Iterator<IN> iterator;
    private OUT next;

    protected FilterTransformIterator(Iterator<@NotNull IN> upstream) {
        this.iterator = upstream;
        this.next = findNext();
    }

    @Nullable
    private OUT findNext() {
        while (iterator.hasNext()) {
            OUT potentialNext = filterTransform(iterator.next());
            if (potentialNext != null) {
                return potentialNext;
            }
        }

        return null;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public OUT next() {
        if (next != null) {
            OUT thisNext = next;
            next = findNext();
            return thisNext;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Filter & transform the result
     *
     * @param value upstream next value
     *
     * @return null to skip, nonnull to transform
     */
    protected abstract @Nullable OUT filterTransform(IN value);
}