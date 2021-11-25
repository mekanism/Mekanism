package mekanism.api.gear;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Helper class for interacting with and creating custom modules. Get an instance from {@link mekanism.api.MekanismAPI#getModuleHelper()}.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    Collection<ModuleData<?>> loadAllTypes(ItemStack container);

    /**
     * Helper method to create a HUD element with a given icon, text, and color.
     *
     * @param icon  Element icon.
     * @param text  Text to display.
     * @param color Color to render the icon and text in.
     *
     * @return A new HUD element.
     */
    IHUDElement hudElement(ResourceLocation icon, ITextComponent text, HUDColor color);

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
}