package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_MODULE_HELPER)
public class CrTModuleHelper {

    /**
     * Gets all the module types a given item support.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Set of supported module types.
     */
    @ZenCodeType.Method
    public static Set<ModuleData<?>> getSupported(ItemStack container) {
        return MekanismAPI.getModuleHelper().getSupported(container);
    }

    /**
     * Helper to get the various items that support a given module type.
     *
     * @param type Module type.
     *
     * @return Set of items that support the given module type.
     */
    @ZenCodeType.Method
    public static Set<Item> getSupported(ModuleData<?> type) {
        return MekanismAPI.getModuleHelper().getSupported(type);
    }

    /**
     * Helper method to check if an item has a module installed and the module is enabled.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     * @param type      Module type.
     *
     * @return {@code true} if the item has the module installed and enabled.
     */
    @ZenCodeType.Method
    public static boolean isEnabled(ItemStack container, ModuleData<?> type) {
        return MekanismAPI.getModuleHelper().isEnabled(container, type);
    }

    /**
     * Helper method to try and load a module from an item.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     * @param type      Module type.
     *
     * @return Module, or {@code null} if no module of the given type is installed.
     */
    @ZenCodeType.Nullable
    @ZenCodeType.Method
    public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> load(ItemStack container, ModuleData<MODULE> type) {
        return MekanismAPI.getModuleHelper().load(container, type);
    }

    /**
     * Gets a list of all modules on an item stack.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return List of modules on an item, or an empty list if the item doesn't support modules.
     */
    @ZenCodeType.Method
    public static List<IModule> loadAll(ItemStack container) {
        //ZenCode does not like ? extends IModule<?> so we need to just cast it to a type without any generics specified
        return (List) MekanismAPI.getModuleHelper().loadAll(container);
    }

    /**
     * Gets all the module types on an item stack.
     *
     * @param container Module container, for example a Meka-Tool or MekaSuit piece.
     *
     * @return Module types on an item.
     */
    @ZenCodeType.Method
    public static Collection<ModuleData<?>> loadAllTypes(ItemStack container) {
        return MekanismAPI.getModuleHelper().loadAllTypes(container);
    }
}