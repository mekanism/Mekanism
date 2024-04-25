package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.common.network.to_client.container.property.IntPropertyData;
import mekanism.common.network.to_client.container.property.ItemStackPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling item stacks
 */
public class SyncableItemStack implements ISyncableData {

    public static SyncableItemStack create(Supplier<@NotNull ItemStack> getter, Consumer<@NotNull ItemStack> setter) {
        return new SyncableItemStack(getter, setter);
    }

    private final Supplier<@NotNull ItemStack> getter;
    private final Consumer<@NotNull ItemStack> setter;
    @NotNull
    private ItemStack lastKnownValue = ItemStack.EMPTY;

    private SyncableItemStack(Supplier<@NotNull ItemStack> getter, Consumer<@NotNull ItemStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @NotNull
    public ItemStack get() {
        return getter.get();
    }

    public void set(@NotNull ItemStack value) {
        setter.accept(value);
    }

    public void set(int amount) {
        ItemStack stack = get();
        if (!stack.isEmpty()) {
            //Double check it is not empty
            stack.setCount(amount);
        }
    }

    @Override
    public DirtyType isDirty() {
        ItemStack value = get();
        if (value.isEmpty() && this.lastKnownValue.isEmpty()) {
            //If they are both empty, we don't need to update anything they are identical
            // Note: isItemEqual returns false if one is empty, even if the other may also be empty
            return DirtyType.CLEAN;
        }
        boolean sameItem = ItemStack.isSameItemSameComponents(value, this.lastKnownValue);
        if (!sameItem || value.getCount() != this.lastKnownValue.getCount()) {
            //Make sure to copy it in case our item stack object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copy();
            return sameItem ? DirtyType.SIZE : DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new IntPropertyData(property, get().getCount());
        }
        //Note: While this copy operation isn't strictly necessary, it allows for simplifying the logic and ensuring we don't have the actual stack object
        // leak from one side to another when in single player. Given copying is rather cheap, and we only need to do this on change/when the data is dirty
        // we can easily get away with it
        return new ItemStackPropertyData(property, get().copy());
    }
}