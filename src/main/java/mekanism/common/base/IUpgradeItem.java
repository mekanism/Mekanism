package mekanism.common.base;

import mekanism.api.Upgrade;
import net.minecraft.item.ItemStack;

//TODO: Move this to the API package, and also replace it with a capability when replacing IGasItem with a capability
public interface IUpgradeItem {

    Upgrade getUpgradeType(ItemStack stack);
}