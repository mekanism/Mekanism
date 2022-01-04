package mekanism.common.item.interfaces;

import mekanism.api.Upgrade;
import net.minecraft.world.item.ItemStack;

//TODO: Move this to the API package, and also replace it with a capability when replacing IGasItem with a capability
public interface IUpgradeItem {

    Upgrade getUpgradeType(ItemStack stack);
}