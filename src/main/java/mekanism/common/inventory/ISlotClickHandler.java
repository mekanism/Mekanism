package mekanism.common.inventory;

import mekanism.common.content.transporter.HashedItem;
import net.minecraft.item.ItemStack;

public interface ISlotClickHandler {

    public void onClick(IScrollableSlot slot, int button, boolean hasShiftDown, ItemStack heldItem);

    public interface IScrollableSlot {

        public HashedItem getItem();

        public long getCount();

        public String getDisplayName();

        public String getModID();
    }
}