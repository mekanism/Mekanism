package mekanism.common;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CreativeTabMekanism extends ItemGroup {

    public CreativeTabMekanism() {
        //TODO: I think this is lang string so rename it to a better format
        super("tabMekanism");
    }

    @Nonnull
    @Override
    public ItemStack createIcon() {
        return MekanismItem.ATOMIC_ALLOY.getItemStack();
    }
}