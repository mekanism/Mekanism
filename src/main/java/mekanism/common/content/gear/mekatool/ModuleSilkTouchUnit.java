package mekanism.common.content.gear.mekatool;

import mekanism.common.content.gear.EnchantmentBasedModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

public class ModuleSilkTouchUnit extends EnchantmentBasedModule<ModuleSilkTouchUnit> {

    @Override
    public Enchantment getEnchantment() {
        return Enchantments.SILK_TOUCH;
    }
}