package mekanism.api.gear;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

/**
 * Abstract implementation to make creating custom modules that provide a specific enchantment when installed easier, while also properly "hiding" the fact that there is
 * an enchantment applied. This does not provide any easy way to make the enchantment use energy or other resources, and is probably only useful for enchantments that
 * have a lot of hardcoded checks so reproducing functionality would be extremely hard if even possible.
 *
 * Instances of this should be returned via the {@link ModuleData}.
 */
@ParametersAreNonnullByDefault
public abstract class EnchantmentBasedModule<MODULE extends EnchantmentBasedModule<MODULE>> implements ICustomModule<MODULE> {

    /**
     * Gets the enchantment that this module provides when enabled.
     *
     * @return The enchantment that this module provides.
     */
    @Nonnull
    public abstract Enchantment getEnchantment();

    @Override
    public void onAdded(IModule<MODULE> module, boolean first) {
        if (module.isEnabled()) {
            if (first) {
                module.getContainer().enchant(getEnchantment(), 1);
            } else {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(module.getContainer());
                enchantments.put(getEnchantment(), module.getInstalledCount());
                EnchantmentHelper.setEnchantments(enchantments, module.getContainer());
            }
        }
    }

    @Override
    public void onRemoved(IModule<MODULE> module, boolean last) {
        if (module.isEnabled()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(module.getContainer());
            if (last) {
                enchantments.remove(getEnchantment());
            } else {
                enchantments.put(getEnchantment(), module.getInstalledCount());
            }
            EnchantmentHelper.setEnchantments(enchantments, module.getContainer());
        }
    }

    @Override
    public void onEnabledStateChange(IModule<MODULE> module) {
        if (module.isEnabled()) {
            //Was disabled and now is enabled, add enchantment
            module.getContainer().enchant(getEnchantment(), module.getInstalledCount());
        } else {
            //Was enabled and is now disabled, remove the enchantment
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(module.getContainer());
            enchantments.remove(getEnchantment());
            EnchantmentHelper.setEnchantments(enchantments, module.getContainer());
        }
    }
}