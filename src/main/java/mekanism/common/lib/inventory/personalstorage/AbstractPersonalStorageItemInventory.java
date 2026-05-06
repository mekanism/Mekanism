package mekanism.common.lib.inventory.personalstorage;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import net.minecraft.util.Util;

@NothingNullByDefault
public abstract class AbstractPersonalStorageItemInventory implements IMekanismInventory {

    protected final List<IInventorySlot> slots = Util.make(new ArrayList<>(), lst -> PersonalStorageManager.createSlots(lst::add, ConstantPredicates.alwaysTrueBi(), this));

    @Override
    public List<IInventorySlot> getContainers() {
        return slots;
    }
}