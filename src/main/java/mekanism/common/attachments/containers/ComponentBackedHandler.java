package mekanism.common.attachments.containers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ComponentBackedHandler<TYPE, CONTAINER extends INBTSerializable<CompoundTag>, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>>
      implements IContentsListener {

    private final List<CONTAINER> containers;
    protected final ItemStack attachedTo;
    private int numNotInitialized;

    //TODO - 1.20.5: Do we want to validate slot indices are within range?
    protected ComponentBackedHandler(ItemStack attachedTo) {
        this.attachedTo = attachedTo;
        ATTACHED attached = getAttached();
        if (attached == null || attached.isEmpty()) {
            //TODO - 1.20.5: Is this meant to be zero if there is no attachment, or is it meant to be what the default is?
            numNotInitialized = 0;
            containers = Collections.emptyList();
        } else {
            numNotInitialized = attached.size();
            //Note: Use an Arrays#asList to allow for null elements and force it to be the size we want it to be
            containers = Arrays.asList((CONTAINER[]) new INBTSerializable[numNotInitialized]);
        }
    }

    protected abstract ContainerType<CONTAINER, ATTACHED, ?> containerType();

    @Nullable
    protected ATTACHED getAttached() {
        return attachedTo.get(containerType().getComponentType());
    }

    public List<CONTAINER> getContainers() {
        //Ensure all our containers are initialized. This short circuits if they are, and if they aren't it initializes any ones that haven't been initialized yet
        for (int i = 0, size = containers.size(); numNotInitialized > 0 && i < size; i++) {
            if (containers.get(i) == null) {
                initializeContainer(i);
            }
        }
        return containers;
    }

    private CONTAINER initializeContainer(int index) {
        //TODO - 1.20.5: ??
        CONTAINER container = containerType().createContainer(attachedTo, index);
        containers.set(index, container);
        numNotInitialized--;
        return container;
    }

    protected CONTAINER getContainer(int index) {
        CONTAINER container = containers.get(index);
        //Lazily initialize the containers
        return container == null ? initializeContainer(index) : container;
    }

    protected int containerCount() {
        ATTACHED attached = getAttached();
        return attached == null ? 0 : attached.size();
    }

    @Override
    public void onContentsChanged() {
    }
}