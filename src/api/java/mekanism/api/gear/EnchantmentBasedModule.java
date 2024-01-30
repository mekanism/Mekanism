package mekanism.api.gear;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Abstract implementation to make creating custom modules that provide a specific enchantment when installed easier, while also properly "hiding" the fact that there is
 * an enchantment applied. This does not provide any easy way to make the enchantment use energy or other resources, and is probably only useful for enchantments that
 * have a lot of hardcoded checks so reproducing functionality would be extremely hard if even possible.
 * <p>
 * Instances of this should be returned via the {@link ModuleData}.
 */
@NothingNullByDefault
public abstract class EnchantmentBasedModule<MODULE extends EnchantmentBasedModule<MODULE>> implements ICustomModule<MODULE> {

    /**
     * Gets the enchantment that this module provides when enabled.
     *
     * @return The enchantment that this module provides.
     */
    public abstract Enchantment getEnchantment();

    @Override
    public void onAdded(IModule<MODULE> module, boolean first) {
        updateEnchantment(module);
    }

    @Override
    public void onRemoved(IModule<MODULE> module, boolean last) {
        updateEnchantment(module);
    }

    @Override
    public void onEnabledStateChange(IModule<MODULE> module) {
        if (module.isEnabled()) {
            //Was disabled and now is enabled, add enchantment
            updateEnchantment(module);
        } else {
            //Was enabled and is now disabled, remove the enchantment
            module.getContainer().setEnchantmentLevel(getEnchantment(), 0);
        }
    }

    private void updateEnchantment(IModule<MODULE> module) {
        if (module.isEnabled()) {
            module.getContainer().setEnchantmentLevel(getEnchantment(), module.getInstalledCount());
        }
    }
}