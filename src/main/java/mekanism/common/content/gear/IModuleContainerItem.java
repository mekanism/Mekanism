package mekanism.common.content.gear;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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

    public default void removeModule(ItemStack stack, ModuleData<?> type) {
        if (hasModule(stack, type)) {
            Module module = getModule(stack, type);
            if (module.getInstalledCount() > 1) {
                module.setInstalledCount(module.getInstalledCount() - 1);
                module.save(null);
            } else {
                ItemDataUtils.getCompound(stack, NBTConstants.MODULES).remove(type.getName());
            }
        }
    }

    public default void addModule(ItemStack stack, ModuleData<?> type) {
        if (hasModule(stack, type)) {
            Module module = getModule(stack, type);
            module.setInstalledCount(module.getInstalledCount() + 1);
            module.save(null);
        } else {
            ItemDataUtils.getCompound(stack, NBTConstants.MODULES).put(type.getName(), new CompoundNBT());
        }
    }
}
