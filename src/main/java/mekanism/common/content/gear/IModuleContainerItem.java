package mekanism.common.content.gear;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;

public interface IModuleContainerItem {

    public default List<Module> getModules(ItemStack stack) {
        return Modules.loadAll(stack);
    }

    public default <MODULE extends Module> MODULE getModule(ItemStack stack, ModuleData<MODULE> type) {
        return Modules.load(stack, type);
    }

    public default boolean hasModule(ItemStack stack, ModuleData<?> type) {
        return ItemDataUtils.getCompound(stack, NBTConstants.MODULES).contains(type.getName());
    }

    public default boolean isModuleEnabled(ItemStack stack, ModuleData<?> type) {
        return hasModule(stack, type) && getModule(stack, type).isEnabled();
    }
}
