package buildcraft.api.items;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public interface IList extends INamedItem {
    boolean matches(@Nonnull ItemStack stackList, @Nonnull ItemStack item);
}
