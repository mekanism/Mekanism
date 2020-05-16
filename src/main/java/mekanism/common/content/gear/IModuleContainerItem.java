package mekanism.common.content.gear;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public interface IModuleContainerItem {

    default List<Module> getModules(ItemStack stack) {
        return Modules.loadAll(stack);
    }

    default <MODULE extends Module> MODULE getModule(ItemStack stack, ModuleData<MODULE> type) {
        return Modules.load(stack, type);
    }

    default boolean hasModule(ItemStack stack, ModuleData<?> type) {
        return ItemDataUtils.getCompound(stack, NBTConstants.MODULES).contains(type.getName());
    }

    default boolean isModuleEnabled(ItemStack stack, ModuleData<?> type) {
        return hasModule(stack, type) && getModule(stack, type).isEnabled();
    }

    default void removeModule(ItemStack stack, ModuleData<?> type) {
        if (hasModule(stack, type)) {
            Module module = getModule(stack, type);
            if (module.getInstalledCount() > 1) {
                module.setInstalledCount(module.getInstalledCount() - 1);
                module.save(null);
                module.onRemoved(false);
            } else {
                ItemDataUtils.getCompound(stack, NBTConstants.MODULES).remove(type.getName());
                module.onRemoved(true);
            }
        }
    }

    default void addModule(ItemStack stack, ModuleData<?> type) {
        if (hasModule(stack, type)) {
            Module module = getModule(stack, type);
            module.setInstalledCount(module.getInstalledCount() + 1);
            module.save(null);
            module.onAdded(false);
        } else {
            if (!ItemDataUtils.hasData(stack, NBTConstants.MODULES, NBT.TAG_COMPOUND)) {
                ItemDataUtils.setCompound(stack, NBTConstants.MODULES, new CompoundNBT());
            }
            ItemDataUtils.getCompound(stack, NBTConstants.MODULES).put(type.getName(), new CompoundNBT());
            Modules.load(stack, type).onAdded(true);
        }
    }
}
