package mekanism.common.content.gear.mekatool;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.common.MekanismLang;

@ParametersAreNotNullByDefault
public class ModuleTeleportationUnit implements ICustomModule<ModuleTeleportationUnit> {

    private IModuleConfigItem<Boolean> requiresBlockTarget;

    @Override
    public void init(IModule<ModuleTeleportationUnit> module, ModuleConfigItemCreator configItemCreator) {
        requiresBlockTarget = configItemCreator.createConfigItem("require_block_target", MekanismLang.MODULE_TELEPORT_REQUIRES_BLOCK, new ModuleBooleanData());
    }

    public boolean requiresBlockTarget() {
        return requiresBlockTarget.get();
    }
}