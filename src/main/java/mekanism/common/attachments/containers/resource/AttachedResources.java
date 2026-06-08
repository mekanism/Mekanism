package mekanism.common.attachments.containers.resource;

import java.util.Collections;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.NonNull;

public record AttachedResources<RESOURCE extends @NonNull Resource>(List<@NonNull LargeResourceStack<RESOURCE>> containers)
      implements IAttachedContainers<LargeResourceStack<RESOURCE>, AttachedResources<RESOURCE>> {

    private static final AttachedResources<?> EMPTY = new AttachedResources<>(Collections.emptyList());

    @SuppressWarnings("unchecked")
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> empty() {
        return (AttachedResources<RESOURCE>) EMPTY;
    }

    @NonNull
    public static <RESOURCE extends @NonNull Resource> AttachedResources<RESOURCE> create(int containers, LargeResourceStack<RESOURCE> emptyStack) {
        return new AttachedResources<>(NonNullList.withSize(containers, emptyStack));
    }

    public AttachedResources {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @NonNull
    @Override
    public AttachedResources<RESOURCE> create(@NonNull List<LargeResourceStack<RESOURCE>> containers) {
        return new AttachedResources<>(containers);
    }
}