package mekanism.common.component.containers.type;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Supplier;
import mekanism.common.component.containers.creator.IContainerCreator;
import mekanism.common.config.IMekanismConfig;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public abstract class AbstractContainerType<CONTAINER extends ValueIOSerializable, ATTACHED> implements IContainerType<CONTAINER, ATTACHED> {

    private final Map<Item, Lazy<? extends IContainerCreator<CONTAINER, ATTACHED>>> knownDefaultCreators = new Reference2ObjectOpenHashMap<>();
    private final DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component;
    private final String containerTag;

    protected AbstractContainerType(DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> component, String containerTag) {
        ContainerType.TYPES_INTERNAL.add(this);
        this.component = component;
        this.containerTag = containerTag;
    }

    @Override
    public DeferredHolder<DataComponentType<?>, DataComponentType<ATTACHED>> getComponentType() {
        return component;
    }

    @Override
    public String getTag() {
        return containerTag;
    }

    /// Adds some containers as default and exposes it as a capability that requires the given configs if the specified bus is present.
    @Override
    public void addDefaultCreators(@Nullable IEventBus eventBus, Item item, Supplier<? extends IContainerCreator<CONTAINER, ATTACHED>> defaultCreator,
          IMekanismConfig... requiredConfigs) {
        knownDefaultCreators.put(item, Lazy.of(defaultCreator));
    }

    @Nullable
    private IContainerCreator<CONTAINER, ATTACHED> getCreator(Item item) {
        Lazy<? extends IContainerCreator<CONTAINER, ATTACHED>> containerCreator = knownDefaultCreators.get(item);
        return containerCreator == null ? null : containerCreator.get();
    }

    @Override
    public int getContainerCount(Item item) {
        IContainerCreator<CONTAINER, ATTACHED> creator = getCreator(item);
        return creator == null ? 0 : creator.totalContainers();
    }

    @Nullable
    @Override
    public ATTACHED createNewAttachment(ItemResource itemType) {
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(itemType.getItem());
        return containerCreator == null ? null : containerCreator.initStorage();
    }

    @Override
    public CONTAINER createContainer(ItemAccess attachedAccess, int containerIndex) {
        //TODO - 1.21: Re-evaluate usages and see if they should be going via capability instead?
        //TODO - 26.1: Theoretically users of this bypass any checks for if the attached access is stacked, but I believe all uses are fine with directly acting on the stack (validate this)
        Item attachedTo = attachedAccess.getResource().getItem();
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(attachedTo);
        if (containerCreator == null) {
            throw new IllegalArgumentException("No known containers for item " + attachedTo);
        }
        return containerCreator.create(attachedAccess, containerIndex);
    }

    @Override
    public boolean supports(Item item) {
        return knownDefaultCreators.containsKey(item);
    }

    @Override
    public void addDefault(Item item, DataComponentMap.Builder components) {
        IContainerCreator<CONTAINER, ATTACHED> containerCreator = getCreator(item);
        if (containerCreator != null) {
            //Supports the type
            ATTACHED attached = containerCreator.initStorage();
            if (shouldAddAttachment(attached)) {
                components.set(getComponentType(), attached);
            }
        }
    }

    protected boolean shouldAddAttachment(ATTACHED attached) {
        return true;
    }
}