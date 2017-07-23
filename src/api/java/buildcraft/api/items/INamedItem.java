package buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface INamedItem {
    String getName(@Nonnull ItemStack stack);

    boolean setName(@Nonnull ItemStack stack, String name);
}
