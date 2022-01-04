package mekanism.common.content.gear.mekasuit;

import javax.annotation.Nonnull;
import mekanism.api.gear.EnchantmentBasedModule;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModuleFrostWalkerUnit extends EnchantmentBasedModule<ModuleFrostWalkerUnit> {

    @Nonnull
    @Override
    public Enchantment getEnchantment() {
        return Enchantments.FROST_WALKER;
    }
}