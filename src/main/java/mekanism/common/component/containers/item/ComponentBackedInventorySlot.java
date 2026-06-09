package mekanism.common.component.containers.item;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.component.containers.resource.ComponentBackedResourceContainer;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.ResourceContainerType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Range;

public class ComponentBackedInventorySlot extends ComponentBackedResourceContainer<ItemResource> implements IInventorySlot {

    private static final LongSupplier ABSOLUTE_MAX_STACK_SIZE = () -> Item.ABSOLUTE_MAX_STACK_SIZE;

    private final boolean obeyStackLimit;

    public ComponentBackedInventorySlot(ItemAccess attachedAccess, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator) {
        this(attachedAccess, slotIndex, canExtract, canInsert, validator, true, ABSOLUTE_MAX_STACK_SIZE);
    }

    public ComponentBackedInventorySlot(ItemAccess attachedAccess, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<ItemResource> validator, boolean obeyStackLimit, LongSupplier limit) {
        super(attachedAccess, slotIndex, canExtract, canInsert, validator, limit, null, null);
        this.obeyStackLimit = obeyStackLimit;
    }

    @Override
    protected ResourceContainerType<ItemResource, IInventorySlot> containerType() {
        return ContainerType.ITEM;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long capacityAsLong(ItemResource resource) {
        //Note: The below logic gracefully handles when zero is returned from super due to the resource not being valid
        long limit = super.capacityAsLong(resource);
        return obeyStackLimit && !resource.isEmpty() ? Math.min(limit, resource.getMaxStackSize()) : limit;
    }
}