package mekanism.common.attachments.containers;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ComponentBackedHandler<TYPE, CONTAINER extends INBTSerializable<CompoundTag>, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>> extends AbstractList<CONTAINER>
      implements IContentsListener {

    protected final ItemStack attachedTo;
    private final int totalContainers;

    @Nullable
    private List<CONTAINER> containers;
    private int numNotInitialized;

    //TODO - 1.21: Do we want to validate slot indices are within range?
    protected ComponentBackedHandler(ItemStack attachedTo, int totalContainers) {
        this.attachedTo = attachedTo;
        this.totalContainers = totalContainers;
    }

    protected abstract ContainerType<CONTAINER, ATTACHED, ?> containerType();

    protected ATTACHED getAttached() {
        return containerType().getOrEmpty(attachedTo);
    }

    protected TYPE getContents(int index) {
        return getAttached().getOrDefault(index);
    }

    private List<CONTAINER> containers() {
        //Lazily initialize the list of containers
        if (containers == null) {
            //Note: Use an Arrays#asList to allow for null elements and force it to be the size we want it to be
            containers = Arrays.asList((CONTAINER[]) new INBTSerializable[totalContainers]);
            numNotInitialized = totalContainers;
        }
        return containers;
    }

    public List<CONTAINER> getContainers() {
        List<CONTAINER> containers = containers();
        //Ensure all our containers are initialized. This short circuits if they are, and if they aren't it initializes any ones that haven't been initialized yet
        for (int i = 0, size = containers.size(); numNotInitialized > 0 && i < size; i++) {
            if (containers.get(i) == null) {
                initializeContainer(i);
            }
        }
        return containers;
    }

    private CONTAINER initializeContainer(int index) {
        //Create a new container for the given index, and set it as initialized
        CONTAINER container = containerType().createContainer(attachedTo, index);
        containers().set(index, container);
        numNotInitialized--;
        return container;
    }

    protected CONTAINER getContainer(int index) {
        CONTAINER container = containers().get(index);
        //Lazily initialize the containers
        return container == null ? initializeContainer(index) : container;
    }

    @Override
    public int size() {
        return totalContainers;
    }

    @Override
    public CONTAINER get(int index) {
        return getContainer(index);
    }

    @Override
    public void onContentsChanged() {
    }

    @Override
    public Iterator<CONTAINER> iterator() {
        return new ContainerIterator();
    }

    private class ContainerIterator implements Iterator<CONTAINER> {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public CONTAINER next() {
            return getContainer(cursor++);
        }
    }
}