package mekanism.common.lib.inventory.personalstorage;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import net.minecraft.util.Util;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault
public abstract class AbstractPersonalStorageItemInventory implements IMekanismResourceHandler<ItemResource, IInventorySlot> {

    protected final List<IInventorySlot> slots = Util.make(new ArrayList<>(), lst -> PersonalStorageManager.createSlots(lst::add, ConstantPredicates.alwaysTrueBi(), this));

    @Override
    public List<IInventorySlot> getContainers() {
        return slots;
    }
}