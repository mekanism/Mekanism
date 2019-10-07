package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.IItemTier;

//TODO: Set the proper defaults, as I think some numbers changed in 1.14
//TODO: Both IItemTier and IArmorMaterial have get getEnchantability and getRepairMaterial
// This makes it not work properly outside of a dev environment as they have different obfuscated names
public interface IMekanismMaterial extends IItemTier, IArmorMaterial {

    int getSwordDamage();

    float getSwordAtkSpeed();

    float getShovelDamage();

    float getShovelAtkSpeed();

    float getAxeDamage();

    float getAxeAtkSpeed();

    int getPickaxeDamage();

    float getPickaxeAtkSpeed();

    float getHoeAtkSpeed();

    float getPaxelDamage();

    float getPaxelAtkSpeed();

    int getPaxelHarvestLevel();

    @Nonnull
    String getRegistryPrefix();
}