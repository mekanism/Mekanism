package mekanism.common.lib.inventory.personalstorage;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.util.Util;
import net.neoforged.neoforge.transfer.item.ItemResource;

public abstract class AbstractPersonalStorageItemInventory implements IMekanismResourceHandler<ItemResource, IInventorySlot>, IContentsListener {

    protected final List<IInventorySlot> slots = Util.make(new ArrayList<>(), lst -> PersonalStorageManager.createSlots(lst::add, ConstantPredicates.alwaysTrueBi(), this));

    @Override
    public List<IInventorySlot> getContainers() {
        return slots;
    }

    public List<LargeResourceStack<ItemResource>> getNonEmptyContents() {
        return slots.stream().filter(slot -> !slot.isEmpty()).map(IResourceContainer::asStack).toList();
    }
}