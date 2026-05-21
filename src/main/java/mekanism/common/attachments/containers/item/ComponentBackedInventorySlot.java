package mekanism.common.attachments.containers.item;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedInventorySlot extends ComponentBackedResourceContainer<ItemResource> implements IInventorySlot {

    private final boolean obeyStackLimit;

    public ComponentBackedInventorySlot(ItemAccess attachedAccess, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator) {
        this(attachedAccess, slotIndex, canExtract, canInsert, validator, true, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public ComponentBackedInventorySlot(ItemAccess attachedAccess, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator, boolean obeyStackLimit, @Range(from = 0, to = Long.MAX_VALUE) long limit) {
        //TODO - 26.1: Re-evaluate how we are doing the rate and limit for this
        super(attachedAccess, slotIndex, canExtract, canInsert, validator, () -> Integer.MAX_VALUE, () -> limit);
        this.obeyStackLimit = obeyStackLimit;
    }

    @Override
    protected ContainerType<?, AttachedResources<ItemResource>, ?> containerType() {
        return ContainerType.ITEM;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(ItemResource resource) {
        long limit = super.capacityAsLong(resource);
        return obeyStackLimit && !resource.isEmpty() ? Math.min(limit, resource.getMaxStackSize()) : limit;
    }
}