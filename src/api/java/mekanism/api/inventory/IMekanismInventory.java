package mekanism.api.inventory;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IMekanismResourceHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault//TODO - 26.1: Docs and generify to support other resource types
public interface IMekanismInventory extends IMekanismResourceHandler<ItemResource, IInventorySlot> {

    /**
     * Used to check if an instance of {@link IMekanismInventory} actually has an inventory.
     *
     * @return True if we are actually an inventory.
     *
     * @apiNote If for some reason you are comparing to {@link IMekanismInventory} without having gotten the object via the item handler capability, then you must call
     * this method to make sure that it really is an inventory. As most mekanism tiles have this class in their hierarchy.
     * @implNote If this returns false the capability should not be exposed AND methods should turn reasonable defaults for not doing anything.
     */
    default boolean hasInventory() {
        return true;
    }

    //@Override
    default void setStackInSlot(int slot, ItemResource itemType, int amount) {//TODO - 26.1: Re-evaluate, previously was in IItemHandlerModifiable
        IInventorySlot inventorySlot = getContainer(slot);
        if (inventorySlot != null) {
            inventorySlot.setContents(itemType, amount);
        }
    }

    //@Override
    default ItemStack getStackInSlot(int slot) {//TODO - 26.1: Re-evaluate this method
        IInventorySlot inventorySlot = getContainer(slot);
        return inventorySlot == null ? ItemStack.EMPTY : inventorySlot.getResource().toStack(inventorySlot.amount());
    }

    @Override
    default ItemResource getEmptyResource() {
        return ItemResource.EMPTY;
    }

    /**
     * Sided inventory helper for isEmpty
     *
     * @return true if completely empty on the default side
     *
     * @since 10.4.0
     */
    default boolean isInventoryEmpty() {//TODO - 26.1: Potentially rename this to isEmpty and move it to IMekanismResourceHandler?
        for (IInventorySlot slot : getContainers()) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}