package buildcraft.api.items;

import net.minecraft.item.ItemStack;

public interface IList extends INamedItem {
    @Deprecated
    String getLabel(ItemStack stack);

    boolean matches(ItemStack stackList, ItemStack item);
}
