package mekanism.common.attachments.containers.item;

import com.mojang.serialization.Codec;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.LargeResourceStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ComponentBackedResourceContainer;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedInventorySlot extends ComponentBackedResourceContainer<ItemResource> implements IInventorySlot {

    private final boolean obeyStackLimit;

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator) {
        this(attachedTo, slotIndex, canExtract, canInsert, validator, true, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public ComponentBackedInventorySlot(ItemStack attachedTo, int slotIndex, BiPredicate<ItemResource, AutomationType> canExtract,
          BiPredicate<ItemResource, AutomationType> canInsert, Predicate<@NotNull ItemResource> validator, boolean obeyStackLimit,
          @Range(from = 0, to = Long.MAX_VALUE) long limit) {
        super(attachedTo, slotIndex, limit, canExtract, canInsert, validator);
        this.obeyStackLimit = obeyStackLimit;
    }

    @Override
    protected ItemResource getEmptyResource() {
        return ItemResource.EMPTY;
    }

    @Override
    protected Codec<LargeResourceStack<ItemResource>> getResourceStackCodec() {
        return SerializerHelper.ITEM_RESOURCE_STACK_CODEC;
    }

    @Override
    protected ContainerType<?, AttachedResources<ItemResource>, ?> containerType() {
        return ContainerType.ITEM;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getLimitAsLong(ItemResource resource) {
        long limit = super.getLimitAsLong(resource);
        return obeyStackLimit && !resource.isEmpty() ? Math.min(limit, resource.getMaxStackSize()) : limit;
    }
}