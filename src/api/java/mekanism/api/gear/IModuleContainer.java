package mekanism.api.gear;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/**
 * Represents an item that can contain modules. Do not implement this interface directly, register new containers via
 * {@link mekanism.api.MekanismIMC#addModuleContainer(ModuleContainerTarget)}. Module containers are immutable.
 *
 * @since 10.5.0
 */
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

    /**
     * {@return all the enchantments provided by installed modules}
     */
    ItemEnchantments moduleBasedEnchantments();

    /**
     * {@return the level provided by modules for the given enchantment, or zero if the enchantment isn't provided by any modules}
     */
    default int getModuleEnchantmentLevel(Holder<Enchantment> enchantment) {
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
     * @param type Module type.
     *
     * @since 10.7.11
     */
    default int installedCount(Holder<ModuleData<?>> type) {
        IModule<?> module = get(type);
        return module == null ? 0 : module.getInstalledCount();
    }

    /**
     * {@return the module if it is installed in this container}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> get(DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        return getUnchecked(type);
    }

    /**
     * {@return the module if it is installed in this container}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getUnchecked(Holder<ModuleData<?>> type);

    /**
     * {@return the module if it is installed in this container}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    IModule<?> get(Holder<ModuleData<?>> type);

    /**
     * {@return the module if it is installed in this container and is currently enabled}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModule<MODULE> module = get(type);
        return module != null && module.isEnabled() ? module : null;
    }

    /**
     * {@return the module if it is installed in this container and is currently enabled}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    default IModule<?> getIfEnabled(Holder<ModuleData<?>> type) {
        IModule<?> module = get(type);
        return module != null && module.isEnabled() ? module : null;
    }

    /**
     * {@return whether the given module is installed in this container}
     *
     * @param type Module type.
     *
     * @since 10.7.11
     */
    default boolean has(Holder<ModuleData<?>> type) {
        return typedModules().containsKey(type.value());
    }

    /**
     * {@return whether the given module is installed in this container and is enabled}
     *
     * @param typeProvider Module type.
     *
     * @since 10.7.11
     */
    default boolean hasEnabled(Holder<ModuleData<?>> typeProvider) {
        return getIfEnabled(typeProvider) != null;
    }

    /// Gets all the HUD elements that should be displayed when the MekaSuit is rendering the HUD.
    ///
    /// @param player   Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
    /// @param instance The item instance the container is stored on.
    <ITEM extends TypedInstance<Item> & DataComponentGetter> List<IHUDElement> getHUDElements(Player player, ITEM instance);

    /// Gets all the text that should be displayed on the HUD.
    ///
    /// @param player   Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
    /// @param instance The item instance the container is stored on.
    ///
    /// @apiNote These strings will be rendered without requiring the MekaSuit to be worn unlike [#getHUDElements(Player, TypedInstance)].
    <ITEM extends TypedInstance<Item> & DataComponentGetter> List<Component> getHUDStrings(Player player, ITEM instance);
}