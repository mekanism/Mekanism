package mekanism.common.inventory;

import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.item.ItemStack;

public interface ISlotClickHandler {

    void onClick(IScrollableSlot slot, int button, boolean func_231173_s_, ItemStack heldItem);

    interface IScrollableSlot {

        HashedItem getItem();

        long getCount();

        String getDisplayName();

        String getModID();
    }
}