package mekanism.common.item;

import mekanism.common.content.gear.IModuleItem;
import mekanism.common.content.gear.Modules.ModuleData;
import net.minecraft.item.Item;

public class ItemModule extends Item implements IModuleItem {

    private ModuleData<?> moduleData;

    public ItemModule(Properties properties, ModuleData<?> moduleData) {
        super(properties.maxStackSize(moduleData.getMaxStackSize()));
        this.moduleData = moduleData;
    }

    @Override
    public ModuleData<?> getModuleData() {
        return moduleData;
    }
}
