package mekanism.api.gear;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Abstract implementation to make creating custom modules that provide a specific enchantment when installed easier, while also properly "hiding" the fact that there is
 * an enchantment applied. This does not provide any easy way to make the enchantment use energy or other resources, and is probably only useful for enchantments that
 * have a lot of hardcoded checks so reproducing functionality would be extremely hard if even possible.
 * <p>
 * Instances of this should be returned via the {@link ModuleData}.
 *
 * @since 10.6.0
 */
@NothingNullByDefault//TODO - 1.20.5: Update docs on this
public interface EnchantmentAwareModule<MODULE extends EnchantmentAwareModule<MODULE>> extends ICustomModule<MODULE> {

    /**
     * Gets the enchantment that this module provides when enabled.
     *
     * @return The enchantment that this module provides.
     */
    Enchantment enchantment();

    //TODO - 1.20.5: Docs
    default int getLevelFor(IModule<MODULE> module) {
        return module.isEnabled() ? module.getInstalledCount() : 0;
    }
}