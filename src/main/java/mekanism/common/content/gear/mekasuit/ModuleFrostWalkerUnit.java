package mekanism.common.content.gear.mekasuit;

import javax.annotation.Nonnull;
import mekanism.api.gear.EnchantmentBasedModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;

public class ModuleFrostWalkerUnit extends EnchantmentBasedModule<ModuleFrostWalkerUnit> {

    @Nonnull
    @Override
    public Enchantment getEnchantment() {
        return Enchantments.FROST_WALKER;
    }
}