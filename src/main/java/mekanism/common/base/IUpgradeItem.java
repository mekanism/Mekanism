package mekanism.common.base;

import mekanism.api.Upgrade;
import net.minecraft.item.ItemStack;

public interface IUpgradeItem {

    Upgrade getUpgradeType(ItemStack stack);
}