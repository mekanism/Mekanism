package mekanism.api.gear;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
     * Adds or removes the given enchantment as being provided at the given level by a module on this container.
     *
     * @param enchantment Enchantment to set or remove
     * @param level       Enchantment level. A value of zero will remove the enchantment from this container.
     *
     * @apiNote In general this method should <strong>NEVER</strong> be called directly and should be handled by {@link EnchantmentBasedModule}
     */
    @Internal
    void setEnchantmentLevel(Enchantment enchantment, int level);

    /**
     * {@return a new stack/copy representing a copy of the backing stack for use in creating previews that won't modify the backing stack}
     */
    ItemStack getPreviewStack();

    /**
     * Gets a capability that is on the backing stack, useful for when interacting with fluid or chemical contents.
     *
     * @param capability Capability to look up.
     * @param context    Capability context.
     */
    @Nullable <T, C> T getCapabilityFromStack(ItemCapability<T, C> capability, @UnknownNullability C context);

    /**
     * Gets a capability that is on the backing stack, useful for when interacting with fluid or chemical contents.
     *
     * @param capability Capability to look up.
     */
    @Nullable
    default <T> T getCapabilityFromStack(ItemCapability<T, Void> capability) {
        return getCapabilityFromStack(capability, null);
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
     * Helper to check if the stack behind this container is on cooldown for the given player.
     *
     * @param player Player to check the cooldowns of.
     *
     * @return {@code true} if the stack behind this container is on cooldown.
     */
    boolean isContainerOnCooldown(Player player);

    /**
     * {@return if the item for the stack behind this container is an instance of the given class}
     *
     * @param clazz Class to check.
     */
    boolean isInstance(Class<?> clazz);

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
     * {@return all the module types this container supports}
     */
    Set<ModuleData<?>> supportedTypes();

    /**
     * {@return if this container supports the given module type}
     *
     * @param typeProvider Module type
     */
    default boolean supports(IModuleDataProvider<?> typeProvider) {
        return supportedTypes().contains(typeProvider.getModuleData());
    }

    /**
     * Gets all the HUD elements that should be displayed when the MekaSuit is rendering the HUD.
     *
     * @param player Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
     */
    List<IHUDElement> getHUDElements(Player player);

    /**
     * Gets all the text that should be displayed on the HUD.
     *
     * @param player Player using or wearing the container. In general this will be the client player, but is passed to make sidedness safer and easier.
     *
     * @apiNote These strings will be rendered without requiring the MekaSuit to be worn unlike {@link #getHUDElements(Player)}.
     */
    List<Component> getHUDStrings(Player player);
}