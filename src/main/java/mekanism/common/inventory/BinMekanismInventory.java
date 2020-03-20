package mekanism.common.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.tier.BinTier;
import net.minecraft.item.ItemStack;

public class BinMekanismInventory extends ItemStackMekanismInventory {

    private BinInventorySlot binSlot;

    private BinMekanismInventory(@Nonnull ItemStack stack) {
        super(stack);
    }

    @Nonnull
    @Override
    protected List<IInventorySlot> getInitialInventory() {
        binSlot = BinInventorySlot.create(this, Attribute.getTier(((ItemBlockBin) stack.getItem()).getBlock(), BinTier.class));
        return Collections.singletonList(binSlot);
    }

    @Nullable
    public static BinMekanismInventory create(@Nonnull ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemBlockBin) {
            return new BinMekanismInventory(stack);
        }
        return null;
    }

    public BinInventorySlot getBinSlot() {
        return binSlot;
    }
}