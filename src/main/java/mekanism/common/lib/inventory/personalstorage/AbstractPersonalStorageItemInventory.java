package mekanism.common.lib.inventory.personalstorage;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class AbstractPersonalStorageItemInventory implements IMekanismInventory {

    protected final List<IInventorySlot> slots = Util.make(new ArrayList<>(), this::createInventorySlots);

    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slots;
    }

    //todo combine this with the one in the Block Entities?
    private void createInventorySlots(List<IInventorySlot> slots) {
        for (int slotY = 0; slotY < 6; slotY++) {
            for (int slotX = 0; slotX < 9; slotX++) {
                //Note: we allow access to the slots from all sides as long as it is public, unlike in 1.12 where we always denied the bottom face
                // We did that to ensure that things like hoppers that could check IInventory did not bypass any restrictions
                slots.add(BasicInventorySlot.at(this, 8 + slotX * 18, 18 + slotY * 18));
            }
        }
    }
}
