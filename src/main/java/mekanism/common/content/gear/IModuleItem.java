package mekanism.common.content.gear;

import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;

public interface IModuleItem {

    Holder<ModuleData<?>> getModuleData();
}