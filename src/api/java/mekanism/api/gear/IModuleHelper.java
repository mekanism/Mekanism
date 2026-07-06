package mekanism.api.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import mekanism.api.IDynamicItemHelper;
import mekanism.api.MekanismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/// Helper class for interacting with and creating custom modules.
///
/// @see IModuleHelper#INSTANCE
public interface IModuleHelper extends IDynamicItemHelper<ModuleData<?>> {

    /// Provides access to Mekanism's implementation of [IModuleHelper].
    ///
    /// @since 10.4.0
    IModuleHelper INSTANCE = MekanismAPI.getService(IModuleHelper.class);

    /// Helper method to add an empty component to represent an empty module container.
    ///
    /// @param properties Properties for the item.
    ///
    /// @return The properties with the component for no stored modules in place
    ///
    /// @since 10.6.0
    Item.Properties applyModuleContainerProperties(Item.Properties properties);

    /// Helper to drop any modules stored in a custom module container. Call this from [Item#onDestroyed(ItemEntity, DamageSource)].
    ///
    /// @param entity Entity that is being destroyed.
    /// @param source Damage source that destroyed the entity.
    ///
    /// @since 10.5.3
    void dropModuleContainerContents(ItemEntity entity, DamageSource source);

    /// Gets all the module types a given item support.
    ///
    /// @param item Module container, for example a Meka-Tool or MekaSuit piece.
    ///
    /// @return Set of supported module types.
    Set<ModuleData<?>> getSupported(Item item);

    /// Gets all the module types a given item support.
    ///
    /// @param item Module container, for example a Meka-Tool or MekaSuit piece.
    ///
    /// @return Set of supported module types.
    ///
    /// @since 10.7.11
    default Set<ModuleData<?>> getSupported(Holder<Item> item) {
        return getSupported(item.value());
    }

    /// Helper to get the various items that support a given module type.
    ///
    /// @param type Module type.
    ///
    /// @return Set of items that support the given module type.
    ///
    /// @since 10.7.11
    Set<Item> getSupportedItems(Holder<ModuleData<?>> type);

    /// {@return if the module container supports the given module type}
    ///
    /// @param item Module container, for example a Meka-Tool or MekaSuit piece.
    /// @param type Module type
    ///
    /// @since 10.7.11
    default boolean supports(Holder<Item> item, Holder<ModuleData<?>> type) {
        return getSupported(item).contains(type.value());
    }

    /// Gets all the module types a given module type conflicts with.
    ///
    /// @param type Module type.
    ///
    /// @return Set of conflicting module types.
    ///
    /// @since 10.7.11
    Set<ModuleData<?>> getConflicting(Holder<ModuleData<?>> type);

    /// Helper method to check if an item has a module installed and the module is enabled.
    ///
    /// @param instance Module container, for example a Meka-Tool or MekaSuit piece.
    /// @param type     Module type.
    ///
    /// @return `true` if the item has the module installed and enabled.
    ///
    /// @since 10.7.11
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean isEnabled(ITEM instance, Holder<ModuleData<?>> type) {
        IModuleContainer container = getModuleContainer(instance);
        return container != null && container.hasEnabled(type);
    }

    /// Helper method to try and load a module from an item.
    ///
    /// @param instance Module container, for example a Meka-Tool or MekaSuit piece.
    /// @param type     Module type.
    ///
    /// @return Module, or `null` if no module of the given type is installed.
    ///
    /// @since 10.7.11
    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ITEM instance,
          DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(instance);
        return container == null ? null : container.get(type);
    }

    /// {@return the module if it is installed on the given item and is currently enabled}
    ///
    /// @param instance Item instance to check for being a module container and then to retrieve the container of.
    /// @param type     Module type.
    ///
    /// @since 10.7.11
    @Nullable
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(ITEM instance,
          DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(instance);
        return container == null ? null : container.getIfEnabled(type);
    }

    /// {@return the module if it is installed on the item in entity's equipment slot and is currently enabled}
    ///
    /// @param entity Entity that has the stack.
    /// @param slot   Slot the stack is in.
    /// @param type   Module type.
    ///
    /// @since 10.7.11
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot,
          DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(entity, slot);
        return container == null ? null : container.getIfEnabled(type);
    }

    /// {@return module container for the item, or null if the item is empty or not a module container}
    ///
    /// @param instance Item instance to check for being a module container and then to retrieve the container of.
    ///
    /// @since 10.5.15
    @Nullable
    <ITEM extends TypedInstance<Item> & DataComponentGetter> IModuleContainer getModuleContainer(ITEM instance);

    /// {@return module container for the item in entity's equipment slot, or null if the entity is null, or the item is empty or not a module container}
    ///
    /// @param entity Entity that has the stack.
    /// @param slot   Slot the stack is in.
    ///
    /// @since 10.5.15
    @Nullable
    default IModuleContainer getModuleContainer(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot) {
        if (entity == null || slot == null) {
            return null;
        }
        return getModuleContainer(entity.getItemBySlot(slot));
    }

    /// Checks if the item is a module container and can store modules.
    ///
    /// @param typedInstance Typed instance containing the item to check.
    ///
    /// @return `true` if the typedInstance is a module container.
    ///
    /// @since 10.5.0
    default boolean isModuleContainer(TypedInstance<Item> typedInstance) {
        return !typedInstance.is(BlockItemIds.AIR.item()) && isModuleContainer(typedInstance.typeHolder());
    }

    /// Checks if the item is a module container and can store modules.
    ///
    /// @param item Item to check.
    ///
    /// @return `true` if the item is a module container.
    ///
    /// @since 10.5.0
    boolean isModuleContainer(Item item);

    /// Checks if the item is a module container and can store modules.
    ///
    /// @param item Item to check.
    ///
    /// @return `true` if the item is a module container.
    ///
    /// @since 10.7.11
    default boolean isModuleContainer(Holder<Item> item) {
        return isModuleContainer(item.value());
    }

    /// {@return all the installed modules on an item, or empty if the item doesn't support modules}
    ///
    /// @param instance Module container, for example a Meka-Tool or MekaSuit piece.
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> Collection<? extends IModule<?>> getAllModules(ITEM instance) {
        IModuleContainer container = getModuleContainer(instance);
        return container == null ? Collections.emptyList() : container.modules();
    }

    /// Gets a list of all modules on an item that have a custom module matching a given class.
    ///
    /// @param instance    Module container, for example a Meka-Tool or MekaSuit piece.
    /// @param moduleClass Class representing the type of module's to load.
    ///
    /// @return List of modules on an item of the given class, or an empty list if the item doesn't support modules or has no modules of that type.
    @SuppressWarnings("unchecked")
    default <ITEM extends TypedInstance<Item> & DataComponentGetter, MODULE extends ICustomModule<?>> List<? extends IModule<? extends MODULE>> getAllModules(ITEM instance,
          Class<MODULE> moduleClass) {
        List<IModule<? extends MODULE>> list = new ArrayList<>();
        for (IModule<?> module : getAllModules(instance)) {
            if (moduleClass.isInstance(module.getCustomInstance())) {
                list.add((IModule<? extends MODULE>) module);
            }
        }
        return list;
    }

    /// Gets all the module types on an item.
    ///
    /// @param instance Module container, for example a Meka-Tool or MekaSuit piece.
    ///
    /// @return Module types on an item.
    default <ITEM extends TypedInstance<Item> & DataComponentGetter> Set<ModuleData<?>> getAllTypes(ITEM instance) {
        IModuleContainer container = getModuleContainer(instance);
        return container == null ? Collections.emptySet() : container.moduleTypes();
    }
}