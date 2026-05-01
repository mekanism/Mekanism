package mekanism.common.capabilities.proxy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ProxyItemHandler extends ProxyHandler implements ResourceHandler<ItemResource> {

    private final IMekanismInventory inventory;

    public ProxyItemHandler(IMekanismInventory inventory, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public ItemResource getResource(int index) {
        return inventory.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        return inventory.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return inventory.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !readOnly || inventory.isValid(index, resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return readOnlyInsert() ? 0 : inventory.insert(index, resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return readOnlyInsert() ? 0 : inventory.insert(resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0 : inventory.extract(index, resource, amount, transaction, AutomationType.handler(side));
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return readOnlyExtract() ? 0 : inventory.extract(resource, amount, transaction, AutomationType.handler(side));
    }

    //TODO - 26.1: Re-evaluate this
    /*@Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!readOnly) {
            inventory.setStackInSlot(slot, stack);
        }
    }*/
}