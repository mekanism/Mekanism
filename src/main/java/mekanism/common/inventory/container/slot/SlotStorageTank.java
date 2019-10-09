package mekanism.common.inventory.container.slot;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.IGasItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

//TODO: Re-evaluate this
public class SlotStorageTank extends Slot {

    private Collection<@NonNull Gas> types;
    private boolean acceptsAllGasses;

    public SlotStorageTank(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        types = Collections.emptyList();
        acceptsAllGasses = true;
    }

    public SlotStorageTank(IInventory inventory, @Nonnull Gas gas, boolean all, int index, int x, int y) {
        super(inventory, index, x, y);
        types = Collections.singletonList(gas);
        acceptsAllGasses = all;
    }

    public SlotStorageTank(IInventory inventory, Collection<@NonNull Gas> gases, boolean all, int index, int x, int y) {
        super(inventory, index, x, y);
        types = gases;
        acceptsAllGasses = all;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack) {
        if (acceptsAllGasses) {
            return itemstack.getItem() instanceof IGasItem;
        }
        if (itemstack.getItem() instanceof IGasItem) {
            IGasItem gasItem = (IGasItem) itemstack.getItem();
            return gasItem.getGas(itemstack).isEmpty() || types.contains(gasItem.getGas(itemstack).getType());
        }
        return false;
    }
}