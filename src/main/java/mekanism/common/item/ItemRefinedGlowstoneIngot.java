package mekanism.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRefinedGlowstoneIngot extends Item {

    public ItemRefinedGlowstoneIngot(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        return true;
    }
}