package mekanism.common.base;

import mekanism.common.Upgrade;
import net.minecraft.item.ItemStack;

public interface IUpgradeItem {

    Upgrade getUpgradeType(ItemStack stack);
}
