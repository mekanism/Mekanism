package buildcraft.api.items;

import net.minecraft.item.ItemStack;

public interface IList extends INamedItem {
    boolean matches(ItemStack stackList, ItemStack item);
}
