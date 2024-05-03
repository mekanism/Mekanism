package mekanism.api.gear;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.config.ModuleConfig;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an item that can contain modules. Do not implement this interface directly, register new containers via
 * {@link mekanism.api.MekanismIMC#addModuleContainer(ModuleContainerTarget)}.
 *
 * @since 10.5.0
 */
@NothingNullByDefault
public interface IModuleContainer {

    /**
     * {@return all the modules currently installed on this container mapped by their type}
     */
    Map<ModuleData<?>, ? extends IModule<?>> typedModules();

    /**
     * {@return all the modules currently installed on this container}
     */
    Collection<? extends IModule<?>> modules();

    /**
     * {@return set of module types for the modules currently installed on this container}
     */
    default Set<ModuleData<?>> moduleTypes() {
        return typedModules().keySet();
    }

    //TODO - 1.20.5: Docs
    <MODULE extends ICustomModule<MODULE>> IModuleContainer replaceModuleConfig(ItemStack stack, ModuleData<MODULE> type, ModuleConfig<?> config);

    /**
     * {@return all the enchantments provided by installed modules}
     */
    ItemEnchantments moduleBasedEnchantments();

    /**
     * {@return the level provided by modules for the given enchantment, or zero if the enchantment isn't provided by any modules}
     */
    default int getModuleEnchantmentLevel(Enchantment enchantment) {
        return moduleBasedEnchantments().getLevel(enchantment);
    }

    /**
     * {@return the number of installed module types}
     */
    default int installedCount() {
        return typedModules().size();
    }

    /**
     * {@return the number of modules of a given type that are installed}
     *
     * @param typeProvider Module type.
     */
    default int installedCount(IModuleDataProvider<?> typeProvider) {
        IModule<?> module = get(typeProvider);
        return module == null ? 0 : module.getInstalledCount();
    }

    /**
     * {@return the module if it is installed in this container}
     *
     * @param typeProvider Module type.
     */
    @Nullable <MODULE extends ICustomModule<MODULE>> IModule<MODULE> get(IModuleDataProvider<MODULE> typeProvider);

    /**
     * {@return the module if it is installed in this container and is currently enabled}
     *
     * @param typeProvider Module type.
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(IModuleDataProvider<MODULE> typeProvider) {
        IModule<MODULE> module = get(typeProvider);
        return module != null && module.isEnabled() ? module : null;
    }

    /**
     * {@return whether the given module is installed in this container}
     *
     * @param typeProvider Module type.
     */
    default boolean has(IModuleDataProvider<?> typeProvider) {
        return typedModules().containsKey(typeProvider.getModuleData());
    }

    /**
     * {@return whether the given module is installed in this container and is enabled}
     *
     * @param typeProvider Module type.
     */
    default boolean hasEnabled(IModuleDataProvider<?> typeProvider) {
        return getIfEnabled(typeProvider) != null;
    }

    /**
     * Gets all the HUD elements that should be displayed when the MekaSuit is rendering the HUD.
     *
     * @param player Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
     */
    List<IHUDElement> getHUDElements(Player player, ItemStack stack);//TODO - 1.20.5: Document stack

    /**
     * Gets all the text that should be displayed on the HUD.
     *
     * @param player Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
     *
     * @apiNote These strings will be rendered without requiring the MekaSuit to be worn unlike {@link #getHUDElements(Player, ItemStack)}.
     */
    List<Component> getHUDStrings(Player player, ItemStack stack);//TODO - 1.20.5: Document stack
}