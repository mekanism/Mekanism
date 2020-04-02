package mekanism.common.content.gear;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

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
            if (!ItemDataUtils.hasData(stack, NBTConstants.MODULES, NBT.TAG_COMPOUND)) {
                ItemDataUtils.setCompound(stack, NBTConstants.MODULES, new CompoundNBT());
            }
            ItemDataUtils.getCompound(stack, NBTConstants.MODULES).put(type.getName(), new CompoundNBT());
            // disable other exclusive modules if this is an exclusive module, as this one will now be active
            if (type.isExclusive()) {
                for (Module module : Modules.loadAll(stack)) {
                    if (module.getData() != type && module.getData().isExclusive()) {
                        module.setEnabledNoCheck(false);
                    }
                }
            }
        }
    }
}
