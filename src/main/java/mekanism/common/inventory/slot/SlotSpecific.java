package mekanism.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotSpecific extends Slot
{
    private Class<? extends Item> itemClass;

    public SlotSpecific(IInventory inventory, int index, int x, int y, Class<? extends Item> c)
    {
        super(inventory, index, x, y);

        itemClass = c;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        return itemClass.equals(itemstack.getItem().getClass()) || itemClass.isInstance(itemstack.getItem());
    }
}
