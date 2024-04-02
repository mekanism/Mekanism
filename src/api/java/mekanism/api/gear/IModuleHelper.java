package mekanism.api.gear;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
    IModuleHelper INSTANCE = ServiceLoader.load(IModuleHelper.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IModuleHelper found"));

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
     */
    Item createModuleItem(IModuleDataProvider<?> moduleDataProvider, Item.Properties properties);

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
     * Helper to get the various items that support a given module type.
     *
     * @param typeProvider Module type.
     *
     * @return Set of items that support the given module type.
     */
    Set<Item> getSupported(IModuleDataProvider<?> typeProvider);

    /**
     * Gets all the module types a given module type conflicts with.
     *
     * @param typeProvider Module type.
     *
     * @return Set of conflicting module types.
     *
     * @since 10.2.3
     */
    Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> typeProvider);

    /**
     * Helper method to check if an item has a module installed and the module is enabled.
     *
     * @param stack        Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return {@code true} if the item has the module installed and enabled.
     */
    default boolean isEnabled(ItemStack stack, IModuleDataProvider<?> typeProvider) {
        IModuleContainer container = getModuleContainer(stack).orElse(null);
        return container != null && container.hasEnabled(typeProvider);
    }

    /**
     * Helper method to try and load a module from an item.
     *
     * @param stack        Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return Module, or {@code null} if no module of the given type is installed.
     */
    @Nullable
    default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> load(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
        IModuleContainer container = getModuleContainer(stack).orElse(null);
        return container == null ? null : container.get(typeProvider);
    }

    /**
     * {@return module container for the stack, or empty if it is empty or not a module container}
     *
     * @param stack Stack to check for being a module container and then to retrieve the container of.
     *
     * @since 10.5.0
     */
    Optional<? extends IModuleContainer> getModuleContainer(ItemStack stack);

    /**
     * {@return module container for the item in entity's equipment slot, or empty if it is empty or not a module container}
     *
     * @param entity Entity that has the stack.
     * @param slot   Slot the stack is in.
     *
     * @since 10.5.0
     */
    default Optional<? extends IModuleContainer> getModuleContainer(@Nullable LivingEntity entity, @Nullable EquipmentSlot slot) {
        if (entity == null || slot == null) {
            return Optional.empty();
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
        return !stack.isEmpty() && isModuleContainer(stack.getItem());
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
     * {@return all the installed modules on an item stack, or empty if the item doesn't support modules}
     *
     * @param stack Module container, for example a Meka-Tool or MekaSuit piece.
     */
    default Collection<? extends IModule<?>> loadAll(ItemStack stack) {
        return getModuleContainer(stack)
              .map(IModuleContainer::modules)
              .orElse(List.of());
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
    default <MODULE extends ICustomModule<?>> List<? extends IModule<? extends MODULE>> loadAll(ItemStack stack, Class<MODULE> moduleClass) {
        List<IModule<? extends MODULE>> list = new ArrayList<>();
        for (IModule<?> module : loadAll(stack)) {
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
    default Set<ModuleData<?>> loadAllTypes(ItemStack stack) {
        return getModuleContainer(stack)
              .map(IModuleContainer::moduleTypes)
              .orElse(Set.of());
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
     */
    default void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType) {
        addMekaSuitModuleModelSpec(name, moduleDataProvider, slotType, ConstantPredicates.alwaysTrue());
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
     */
    void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive);
}