package mekanism.common.attachments.containers;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.type.IContainerType;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public abstract class ComponentBackedHandler<TYPE, CONTAINER extends ValueIOSerializable, ATTACHED extends IAttachedContainers<TYPE, ATTACHED>,
      CONTAINER_TYPE extends IContainerType<CONTAINER, ATTACHED>> {

    private final CONTAINER_TYPE containerType;
    protected final ItemAccess attachedAccess;
    private final int totalContainers;

    @Nullable
    private List<CONTAINER> containers;
    private int numNotInitialized;

    //TODO - 1.21: Do we want to validate slot indices are within range?
    protected ComponentBackedHandler(CONTAINER_TYPE containerType, ItemAccess attachedAccess, int totalContainers) {
        this.containerType = containerType;
        this.attachedAccess = attachedAccess;
        this.totalContainers = totalContainers;
    }

    protected final CONTAINER_TYPE containerType() {
        return containerType;
    }

    protected ATTACHED getAttached() {
        return containerType.getOrEmpty(attachedAccess);
    }

    public TYPE getContents(int index) {
        return getAttached().getOrDefault(index);
    }

    private List<CONTAINER> containers() {
        //Lazily initialize the list of containers
        if (containers == null) {
            //Note: Use an Arrays#asList to allow for null elements and force it to be the size we want it to be
            containers = Arrays.asList((CONTAINER[]) new ValueIOSerializable[totalContainers]);
            numNotInitialized = totalContainers;
        }
        return containers;
    }

    protected boolean isAccessInvalid() {
        //TODO - 26.1: Should we have a predicate that checks the item type to see if it is still valid?
        // Probably, or maybe just store the initial item the access was on and only support it changing components but not the core type?
        return attachedAccess.getAmount() == 0;
    }

    public final List<CONTAINER> getContainers() {
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
        CONTAINER container = containerType.createContainer(attachedAccess, index);
        containers().set(index, container);
        numNotInitialized--;
        return container;
    }

    public CONTAINER getContainer(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        List<CONTAINER> containers = containers();
        Objects.checkIndex(index, containers.size());
        CONTAINER container = containers.get(index);
        //Lazily initialize the containers
        return container == null ? initializeContainer(index) : container;
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int size() {
        return totalContainers;
    }
}