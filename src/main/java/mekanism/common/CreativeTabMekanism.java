package mekanism.common;

import javax.annotation.Nonnull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTabMekanism extends CreativeTabs {

    public CreativeTabMekanism() {
        super("tabMekanism");
    }

    @Nonnull
    @Override
    public ItemStack createIcon() {
        return new ItemStack(MekanismItems.AtomicAlloy);
    }
}
