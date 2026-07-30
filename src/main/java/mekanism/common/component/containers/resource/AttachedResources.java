package mekanism.common.component.containers.resource;

import java.util.Collections;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.resource.Resource;

public record AttachedResources<RESOURCE extends Resource>(List<LargeResourceStack<RESOURCE>> containers)
      implements IAttachedContainers<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> {

    private static final AttachedResources<?> EMPTY = new AttachedResources<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <RESOURCE extends Resource> AttachedResources<RESOURCE> empty() {
        return (AttachedResources<RESOURCE>) EMPTY;
    }

    public static <RESOURCE extends Resource> AttachedResources<RESOURCE> create(int containers, LargeResourceStack<RESOURCE> emptyStack) {
        return new AttachedResources<>(NonNullList.withSize(containers, emptyStack));
    }

    public AttachedResources {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public AttachedResources<RESOURCE> create(List<LargeResourceStack<RESOURCE>> containers) {
        return new AttachedResources<>(containers);
    }

    @Override
    public boolean hasNonEmptyContents() {
        for (LargeResourceStack<RESOURCE> container : containers) {
            if (!container.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}