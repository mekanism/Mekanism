package mekanism.common.content.gear;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

@ParametersAreNonnullByDefault
public abstract class EnchantmentBasedModule<MODULE extends EnchantmentBasedModule<MODULE>> implements ICustomModule<MODULE> {

    public abstract Enchantment getEnchantment();

    @Override
    public void onAdded(IModule<MODULE> module, boolean first) {
        if (first) {
            module.getContainer().enchant(getEnchantment(), 1);
        } else {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(module.getContainer());
            enchantments.put(getEnchantment(), module.getInstalledCount());
            EnchantmentHelper.setEnchantments(enchantments, module.getContainer());
        }
    }

    @Override
    public void onRemoved(IModule<MODULE> module, boolean last) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(module.getContainer());
        if (last) {
            enchantments.remove(getEnchantment());
        } else {
            enchantments.put(getEnchantment(), module.getInstalledCount());
        }
        EnchantmentHelper.setEnchantments(enchantments, module.getContainer());
    }
}