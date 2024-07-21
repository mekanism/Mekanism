package mekanism.common.item.gear;

import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;

public class ItemScubaMask extends ItemSpecialArmor {

    public ItemScubaMask(Properties properties) {
        super(MekanismArmorMaterials.SCUBA_GEAR, ArmorItem.Type.HELMET, properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }
}