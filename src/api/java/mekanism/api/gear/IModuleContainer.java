package mekanism.api.gear;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.MekanismIMC.ModuleContainerTarget;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an item that can contain modules. Do not implement this interface directly, register new containers via
 * {@link mekanism.api.MekanismIMC#addModuleContainer(ModuleContainerTarget)}.
 *
 * @since 10.5.0
 */
@NothingNullByDefault
public interface IModuleContainer extends INBTSerializable<CompoundTag> {

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
    boolean has(IModuleDataProvider<?> typeProvider);

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

    /**
     * Checks if this module container is equivalent to another one.
     *
     * @param other Module container to compare to
     *
     * @return {@code true} If this module container can be considered equivalent and compatible with the other module container.
     */
    boolean isCompatible(IModuleContainer other);
}