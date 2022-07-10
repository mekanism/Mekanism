package mekanism.api.gear;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for interacting with and creating custom modules. Get an instance from {@link mekanism.api.MekanismAPI#getModuleHelper()}.
 */
@NothingNullByDefault
public interface IModuleHelper {

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
     * have a {@code null} reference when using {@link net.minecraftforge.registries.DeferredRegister}s where both the {@link ModuleData} and the {@link Item} need
     * references of each other.
     */
    Item createModuleItem(IModuleDataProvider<?> moduleDataProvider, Item.Properties properties);

    /**
     * Gets all the module types a given item support.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Set of supported module types.
     */
    Set<ModuleData<?>> getSupported(ItemStack container);

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
     * @since 10.2.3
     */
    Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> typeProvider);

    /**
     * Helper method to check if an item has a module installed and the module is enabled.
     *
     * @param container    Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return {@code true} if the item has the module installed and enabled.
     */
    boolean isEnabled(ItemStack container, IModuleDataProvider<?> typeProvider);

    /**
     * Helper method to try and load a module from an item.
     *
     * @param container    Module container, for example a Meka-Tool or MekaSuit piece.
     * @param typeProvider Module type.
     *
     * @return Module, or {@code null} if no module of the given type is installed.
     */
    @Nullable
    <MODULE extends ICustomModule<MODULE>> IModule<MODULE> load(ItemStack container, IModuleDataProvider<MODULE> typeProvider);

    /**
     * Gets a list of all modules on an item stack.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return List of modules on an item, or an empty list if the item doesn't support modules.
     */
    List<? extends IModule<?>> loadAll(ItemStack container);

    /**
     * Gets a list of all modules on an item stack that have a custom module matching a given class.
     *
     * @param container   Module container, for example a Meka-Tool or MekaSuit piece.
     * @param moduleClass Class representing the type of module's to load.
     *
     * @return List of modules on an item of the given class, or an empty list if the item doesn't support modules or has no modules of that type.
     */
    <MODULE extends ICustomModule<?>> List<? extends IModule<? extends MODULE>> loadAll(ItemStack container, Class<MODULE> moduleClass);

    /**
     * Gets all the module types on an item stack.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Module types on an item.
     */
    List<ModuleData<?>> loadAllTypes(ItemStack container);

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
     * @param location Asset location assumed to be for an obj file. The {@link ResourceLocation} for the modules Mekanism adds is {@code
     *                 mekanism:models/entity/mekasuit_modules.obj}
     *
     * @apiNote Must only be called on the client side and from {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}.
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
     * @apiNote Must only be called on the client side and from {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}.
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
     * @apiNote Must only be called on the client side and from {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}.
     */
    void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive);
}