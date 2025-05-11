package mekanism.api.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for interacting with and creating custom modules.
 *
 * @see IModuleHelper#INSTANCE
 */
@NothingNullByDefault
public interface IModuleHelper {

    /**
     * Provides access to Mekanism's implementation of {@link IModuleHelper}.
     *
     * @since 10.4.0
     */
    IModuleHelper INSTANCE = MekanismAPI.getService(IModuleHelper.class);

    /**
     * Helper method used to create Module items that can then be registered. When Mekanism is not installed a dummy Item should be registered instead of calling this
     * method.
     *
     * @param moduleDataProvider Module data provider.
     * @param properties         Properties for the item.
     *
     * @return A new item that should be registered during item registration.
     *
     * @apiNote This method specifically uses {@link IModuleDataProvider} rather than {@link java.util.function.Supplier<ModuleData>} to make it harder to accidentally
     * have a {@code null} reference when using {@link DeferredRegister}s where both the {@link ModuleData} and the {@link Item} need references of each other.
     * @deprecated Use {@link #createModuleItem(Supplier, Properties)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default Item createModuleItem(IModuleDataProvider<?> moduleDataProvider, Item.Properties properties) {
        return createModuleItem(() -> asHolder(moduleDataProvider), properties);
    }

    /**
     * Helper method used to create Module items that can then be registered. When Mekanism is not installed a dummy Item should be registered instead of calling this
     * method.
     *
     * @param moduleDataSupplier Module data provider.
     * @param properties         Properties for the item.
     *
     * @return A new item that should be registered during item registration.
     *
     * @since 10.7.11
     */
    Item createModuleItem(Supplier<Holder<ModuleData<?>>> moduleDataSupplier, Item.Properties properties);//TODO - 1.22: Replace this with just taking a holder

    /**
     * Helper method to add an empty component to represent an empty module container.
     *
     * @param properties Properties for the item.
     *
     * @return The properties with the component for no stored modules in place
     *
     * @since 10.6.0
     */
    Item.Properties applyModuleContainerProperties(Item.Properties properties);

    /**
     * Helper to drop any modules stored in a custom module container. Call this from {@link Item#onDestroyed(ItemEntity, DamageSource)}.
     *
     * @param entity Entity that is being destroyed.
     * @param source Damage source that destroyed the entity.
     *
     * @since 10.5.3
     */
    void dropModuleContainerContents(ItemEntity entity, DamageSource source);

    /**
     * Gets all the module types a given item support.
     *
     * @param item Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Set of supported module types.
     */
    Set<ModuleData<?>> getSupported(Item item);

    /**
     * Gets all the module types a given item support.
     *
     * @param item Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Set of supported module types.
     *
     * @since 10.7.11
     */
    default Set<ModuleData<?>> getSupported(Holder<Item> item) {
        return getSupported(item.value());
    }

    /**
     * Helper to get the various items that support a given module type.
     *
     * @param typeProvider Module type.
     *
     * @return Set of items that support the given module type.
     *
     * @deprecated Use {@link #getSupportedItems(Holder)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default Set<Item> getSupported(IModuleDataProvider<?> typeProvider) {
        return getSupportedItems(asHolder(typeProvider));
    }

    /**
     * Helper to get the various items that support a given module type.
     *
     * @param type Module type.
     *
     * @return Set of items that support the given module type.
     *
     * @since 10.7.11
     */
    Set<Item> getSupportedItems(Holder<ModuleData<?>> type);

    /**
     * {@return if the module container supports the given module type}
     *
     * @param item         Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type
     *
     * @since 10.6.0
     * @deprecated Use {@link #supports(Holder, Holder)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default boolean supports(Item item, IModuleDataProvider<?> typeProvider) {
        return getSupported(item).contains(typeProvider.getModuleData());
    }

    /**
     * {@return if the module container supports the given module type}
     *
     * @param item Module container, for example a Meka-Tool or MekaSuit piece.
     * @param type Module type
     *
     * @since 10.7.11
     */
    default boolean supports(Holder<Item> item, Holder<ModuleData<?>> type) {
        return getSupported(item).contains(type.value());
    }

    /**
     * Gets all the module types a given module type conflicts with.
     *
     * @param typeProvider Module type.
     *
     * @return Set of conflicting module types.
     *
     * @since 10.2.3
     * @deprecated Use {@link #getConflicting(Holder)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> typeProvider) {
        return getConflicting(asHolder(typeProvider));
    }

    /**
     * Gets all the module types a given module type conflicts with.
     *
     * @param type Module type.
     *
     * @return Set of conflicting module types.
     *
     * @since 10.7.11
     */
    Set<ModuleData<?>> getConflicting(Holder<ModuleData<?>> type);

    /**
     * Helper method to check if an item has a module installed and the module is enabled.
     *
     * @param stack        Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return {@code true} if the item has the module installed and enabled.
     *
     * @deprecated Use {@link #isEnabled(ItemStack, Holder)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default boolean isEnabled(ItemStack stack, IModuleDataProvider<?> typeProvider) {
        return isEnabled(stack, asHolder(typeProvider));
    }

    /**
     * Helper method to check if an item has a module installed and the module is enabled.
     *
     * @param stack Module container, for example a Meka-Tool or MekaSuit piece.
     * @param type  Module type.
     *
     * @return {@code true} if the item has the module installed and enabled.
     *
     * @since 10.7.11
     */
    default boolean isEnabled(ItemStack stack, Holder<ModuleData<?>> type) {
        IModuleContainer container = getModuleContainer(stack);
        return container != null && container.hasEnabled(type);
    }

    /**
     * Helper method to try and load a module from an item.
     *
     * @param stack        Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return Module, or {@code null} if no module of the given type is installed.
     *
     * @deprecated Use {@link #getModule(ItemStack, DeferredHolder)} instead
     */
    @Nullable
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        IModuleContainer container = getModuleContainer(stack);
        return container == null ? null : container.get(typeProvider);
    }

    /**
     * Helper method to try and load a module from an item.
     *
     * @param stack Module container, for example a Meka-Tool or MekaSuit piece.
     * @param type  Module type.
     *
     * @return Module, or {@code null} if no module of the given type is installed.
     *
     * @since 10.7.11
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ItemStack stack, DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(stack);
        return container == null ? null : container.get(type);
    }

    /**
     * {@return the module if it is installed on the given stack and is currently enabled}
     *
     * @param stack        Stack to check for being a module container and then to retrieve the container of.
     * @param typeProvider Module type.
     *
     * @since 10.5.15
     * @deprecated Use {@link #getIfEnabled(ItemStack, DeferredHolder)} instead
     */
    @Nullable
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        IModuleContainer container = getModuleContainer(stack);
        return container == null ? null : container.getIfEnabled(typeProvider);
    }

    /**
     * {@return the module if it is installed on the given stack and is currently enabled}
     *
     * @param stack Stack to check for being a module container and then to retrieve the container of.
     * @param type  Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(ItemStack stack, DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(stack);
        return container == null ? null : container.getIfEnabled(type);
    }

    /**
     * {@return the module if it is installed on the item in entity's equipment slot and is currently enabled}
     *
     * @param entity       Entity that has the stack.
     * @param slot         Slot the stack is in.
     * @param typeProvider Module type.
     *
     * @since 10.5.15
     * @deprecated Use {@link #getIfEnabled(LivingEntity, EquipmentSlot, DeferredHolder)} instead
     */
    @Nullable
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot,
          IModuleDataProvider<MODULE> typeProvider) {
        IModuleContainer container = getModuleContainer(entity, slot);
        return container == null ? null : container.getIfEnabled(typeProvider);
    }

    /**
     * {@return the module if it is installed on the item in entity's equipment slot and is currently enabled}
     *
     * @param entity Entity that has the stack.
     * @param slot   Slot the stack is in.
     * @param type   Module type.
     *
     * @since 10.7.11
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getIfEnabled(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot,
          DeferredHolder<ModuleData<?>, ModuleData<MODULE>> type) {
        IModuleContainer container = getModuleContainer(entity, slot);
        return container == null ? null : container.getIfEnabled(type);
    }

    /**
     * {@return module container for the stack, or null if the stack is empty or not a module container}
     *
     * @param stack Stack to check for being a module container and then to retrieve the container of.
     *
     * @since 10.5.15
     */
    @Nullable
    IModuleContainer getModuleContainer(ItemStack stack);

    /**
     * {@return module container for the item in entity's equipment slot, or null if the entity is null, or the stack is empty or not a module container}
     *
     * @param entity Entity that has the stack.
     * @param slot   Slot the stack is in.
     *
     * @since 10.5.15
     */
    @Nullable
    default IModuleContainer getModuleContainer(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot) {
        if (entity == null || slot == null) {
            return null;
        }
        return getModuleContainer(entity.getItemBySlot(slot));
    }

    /**
     * Checks if the item is a module container and can store modules.
     *
     * @param stack Stack containing the item to check.
     *
     * @return {@code true} if the stack is a module container.
     *
     * @since 10.5.0
     */
    default boolean isModuleContainer(ItemStack stack) {
        return !stack.isEmpty() && isModuleContainer(stack.getItemHolder());
    }

    /**
     * Checks if the item is a module container and can store modules.
     *
     * @param item Item to check.
     *
     * @return {@code true} if the item is a module container.
     *
     * @since 10.5.0
     */
    boolean isModuleContainer(Item item);

    /**
     * Checks if the item is a module container and can store modules.
     *
     * @param item Item to check.
     *
     * @return {@code true} if the item is a module container.
     *
     * @since 10.7.11
     */
    default boolean isModuleContainer(Holder<Item> item) {
        return isModuleContainer(item.value());
    }

    /**
     * {@return all the installed modules on an item stack, or empty if the item doesn't support modules}
     *
     * @param stack Module container, for example a Meka-Tool or MekaSuit piece.
     */
    default Collection<? extends IModule<?>> getAllModules(ItemStack stack) {
        IModuleContainer container = getModuleContainer(stack);
        return container == null ? Collections.emptyList() : container.modules();
    }

    /**
     * Gets a list of all modules on an item stack that have a custom module matching a given class.
     *
     * @param stack       Module container, for example a Meka-Tool or MekaSuit piece.
     * @param moduleClass Class representing the type of module's to load.
     *
     * @return List of modules on an item of the given class, or an empty list if the item doesn't support modules or has no modules of that type.
     */
    @SuppressWarnings("unchecked")
    default <MODULE extends ICustomModule<?>> List<? extends IModule<? extends MODULE>> getAllModules(ItemStack stack, Class<MODULE> moduleClass) {
        List<IModule<? extends MODULE>> list = new ArrayList<>();
        for (IModule<?> module : getAllModules(stack)) {
            if (moduleClass.isInstance(module.getCustomInstance())) {
                list.add((IModule<? extends MODULE>) module);
            }
        }
        return list;
    }

    /**
     * Gets all the module types on an item stack.
     *
     * @param stack Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Module types on an item.
     */
    default Set<ModuleData<?>> getAllTypes(ItemStack stack) {
        IModuleContainer container = getModuleContainer(stack);
        return container != null ? container.moduleTypes() : Collections.emptySet();
    }

    /**
     * Helper method to create a HUD element with a given icon, text, and color.
     *
     * @param icon  Element icon.
     * @param text  Text to display.
     * @param color Color to render the icon and text in.
     *
     * @return A new HUD element.
     */
    IHUDElement hudElement(ResourceLocation icon, Component text, HUDColor color);

    /**
     * Helper method to create a HUD element representing an enabled state with a given icon.
     *
     * @param icon    Element icon.
     * @param enabled {@code true} if the element should use the enabled text and color, {@code false} if it should use the disabled text and color.
     *
     * @return A new HUD element.
     */
    IHUDElement hudElementEnabled(ResourceLocation icon, boolean enabled);

    /**
     * Helper method to create a HUD element representing a ratio with a given icon.
     *
     * @param icon  Element icon.
     * @param ratio Ratio. Values below 0.1 will display using {@link HUDColor#DANGER}, values above 0.1 and below 0.2 will display using {@link HUDColor#WARNING}, and
     *              values above 0.2 will display using {@link HUDColor#REGULAR}.
     *
     * @return A new HUD element.
     */
    IHUDElement hudElementPercent(ResourceLocation icon, double ratio);

    /**
     * Adds a file that contains overrides and models for some custom modules.
     *
     * @param location Asset location assumed to be for an obj file. The {@link ResourceLocation} for the modules Mekanism adds is
     *                 {@code mekanism:models/entity/mekasuit_modules.obj}
     *
     * @apiNote Must only be called on the client side and from {@link FMLClientSetupEvent}.
     */
    void addMekaSuitModuleModels(ResourceLocation location);

    /**
     * Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled. This method causes the "active" model to
     * always be selected.
     *
     * @param name               Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
     * @param moduleDataProvider {@link ModuleData} to associate this spec with.
     * @param slotType           Equipment position the spec will be used for.
     *
     * @apiNote Must only be called on the client side and from {@link FMLClientSetupEvent}.
     * @see #addMekaSuitModuleModelSpec(String, IModuleDataProvider, EquipmentSlot, Predicate)
     * @deprecated Use {@link #addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot)} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType) {
        addMekaSuitModuleModelSpec(name, moduleDataProvider, slotType, ConstantPredicates.alwaysTrue());
    }

    /**
     * Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled. This method causes the "active" model to
     * always be selected.
     *
     * @param name       Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
     * @param moduleData {@link ModuleData} to associate this spec with.
     * @param slotType   Equipment position the spec will be used for.
     *
     * @apiNote Must only be called on the client side and from {@link FMLClientSetupEvent}.
     * @see #addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot, Predicate)
     * @since 10.7.11
     */
    default void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType) {
        addMekaSuitModuleModelSpec(name, moduleData, slotType, ConstantPredicates.alwaysTrue());
    }

    /**
     * Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled.
     *
     * @param name               Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
     * @param moduleDataProvider {@link ModuleData} to associate this spec with.
     * @param slotType           Equipment position the spec will be used for.
     * @param isActive           Predicate to check if an entity should use the active or inactive model.
     *
     * @apiNote Must only be called on the client side and from {@link FMLClientSetupEvent}.
     * @deprecated Use {{@link #addMekaSuitModuleModelSpec(String, Holder, EquipmentSlot, Predicate)}} instead
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    default void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive) {
        addMekaSuitModuleModelSpec(name, asHolder(moduleDataProvider), slotType, isActive);
    }

    /**
     * Adds a model spec for a specific MekaSuit Module to allow it to render as part of the MekaSuit when installed and enabled.
     *
     * @param name       Unique name that will be checked for in all the module model files. For third party mods it is recommended this contains your modid.
     * @param moduleData {@link ModuleData} to associate this spec with.
     * @param slotType   Equipment position the spec will be used for.
     * @param isActive   Predicate to check if an entity should use the active or inactive model.
     *
     * @apiNote Must only be called on the client side and from {@link FMLClientSetupEvent}.
     * @since 10.7.11
     */
    void addMekaSuitModuleModelSpec(String name, Holder<ModuleData<?>> moduleData, EquipmentSlot slotType, Predicate<LivingEntity> isActive);

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    private Holder<ModuleData<?>> asHolder(IModuleDataProvider<?> moduleDataProvider) {
        return MekanismAPI.MODULE_REGISTRY.wrapAsHolder(moduleDataProvider.getModuleData());
    }
}