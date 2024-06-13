package mekanism.api.gear;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Helper interface to allow for custom modules to apply enchantments when installed or removed. This does not provide any easy way to make the enchantment use energy or
 * other resources, and is probably only useful for enchantments that have a lot of hardcoded checks so reproducing functionality would be extremely hard if even
 * possible.
 * <p>
 * Instances of this should be returned via {@link ModuleData}.
 *
 * @since 10.6.0
 */
@NothingNullByDefault
public interface EnchantmentAwareModule<MODULE extends EnchantmentAwareModule<MODULE>> extends ICustomModule<MODULE> {

    /**
     * Gets the enchantment that this module provides when enabled.
     *
     * @return The enchantment that this module provides.
     */
    ResourceKey<Enchantment> enchantment();

    /**
     * Gets the target enchantment level for this module.
     *
     * @param module Module instance.
     *
     * @return Enchantment level for the current module
     *
     * @implNote This defaults to returning the installed count if the module is enabled, and otherwise returns zero.
     */
    default int getLevelFor(IModule<MODULE> module) {
        return module.isEnabled() ? module.getInstalledCount() : 0;
    }
}