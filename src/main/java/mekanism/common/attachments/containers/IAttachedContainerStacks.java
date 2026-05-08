package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;

@NothingNullByDefault
public interface IAttachedContainerStacks<TYPE, ATTACHED extends IAttachedContainerStacks<TYPE, ATTACHED>> extends IAttachedContainers<TYPE, ATTACHED> {

    TYPE getEmptyStack();

    @Override
    default TYPE getOrDefault(int index) {
        List<TYPE> containers = containers();
        if (index < 0 || index >= containers.size()) {
            return getEmptyStack();
        }
        return containers.get(index);
    }
}