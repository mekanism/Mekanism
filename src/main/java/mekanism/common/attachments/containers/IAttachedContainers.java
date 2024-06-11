package mekanism.common.attachments.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public interface IAttachedContainers<TYPE, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> extends Iterable<TYPE> {

    List<TYPE> containers();

    default boolean isEmpty() {
        return containers().isEmpty();
    }

    default int size() {
        return containers().size();
    }

    default TYPE get(int index) {
        return containers().get(index);
    }

    TYPE getEmptyStack();

    default TYPE getOrDefault(int index) {
        List<TYPE> containers = containers();
        if (index < 0 || index >= containers.size()) {
            return getEmptyStack();
        }
        return containers.get(index);
    }

    ATTACHED create(List<TYPE> containers);

    /**
     * @apiNote It is assumed that data will be copied before calling this method.
     */
    default ATTACHED with(int index, TYPE data) {
        List<TYPE> copy = new ArrayList<>(containers());
        copy.set(index, data);
        return create(copy);
    }

    @Override
    default Iterator<TYPE> iterator() {
        return containers().iterator();
    }

    @Override
    default void forEach(Consumer<? super TYPE> action) {
        containers().forEach(action);
    }

    @Override
    default Spliterator<TYPE> spliterator() {
        return containers().spliterator();
    }
}