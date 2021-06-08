package mekanism.common.content.gear.mekasuit;

import mekanism.common.content.gear.EnchantmentBasedModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

public class ModuleFrostWalkerUnit extends EnchantmentBasedModule<ModuleFrostWalkerUnit> {

    @Override
    public Enchantment getEnchantment() {
        return Enchantments.FROST_WALKER;
    }
}