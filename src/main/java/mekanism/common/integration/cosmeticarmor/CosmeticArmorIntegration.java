package mekanism.common.integration.cosmeticarmor;

import javax.annotation.Nonnull;

import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CosmeticArmorIntegration {

    public static final int CHEST_SLOT = 2;

    public static ItemStack getCosmeticChestItemStack(@Nonnull Player player, @Nonnull ItemStack originalEquipment) {
        InventoryCosArmor invCosArmor = ModObjects.invMan.getCosArmorInventoryClient(player.getUUID());

        if (invCosArmor.isSkinArmor(CHEST_SLOT)) {
            return ItemStack.EMPTY;
        } else {
            ItemStack cosmetic = invCosArmor.getItem(CHEST_SLOT);
            return cosmetic.isEmpty() ? originalEquipment : cosmetic;
        }
    }
}
