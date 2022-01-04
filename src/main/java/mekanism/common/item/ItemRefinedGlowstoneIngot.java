package mekanism.common.item;

import javax.annotation.Nonnull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemRefinedGlowstoneIngot extends Item {

    public ItemRefinedGlowstoneIngot(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPiglinCurrency(@Nonnull ItemStack stack) {
        return true;
    }
}