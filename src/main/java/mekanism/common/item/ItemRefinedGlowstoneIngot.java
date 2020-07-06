package mekanism.common.item;

import javax.annotation.Nonnull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRefinedGlowstoneIngot extends Item {

    public ItemRefinedGlowstoneIngot(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPiglinCurrency(@Nonnull ItemStack stack) {
        return true;
    }
}