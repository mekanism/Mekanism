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

    protected final List<IInventorySlot> slots = Util.make(new ArrayList<>(), lst -> PersonalStorageManager.createSlots(lst::add, BasicInventorySlot.alwaysTrueBi, this));

    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return slots;
    }
}