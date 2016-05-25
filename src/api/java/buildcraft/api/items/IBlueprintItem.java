package buildcraft.api.items;

import net.minecraft.item.ItemStack;

import buildcraft.api.enums.EnumBlueprintType;

public interface IBlueprintItem extends INamedItem {
    EnumBlueprintType getType(ItemStack stack);
}
