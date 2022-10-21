package mekanism.common.content.gear.shared;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleColorData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;

//Note: While it would be nice to make the Meka-Tool support this, there isn't much point as the only spot on the tool that would be colorable
// is the one that actually represents the fingers from the MekaSuit. Unfortunately we don't have access to that context when rendering the
// item variant so for now we just don't handle it
@ParametersAreNotNullByDefault
public class ModuleColorModulationUnit implements ICustomModule<ModuleColorModulationUnit> {

    public static final String COLOR_CONFIG_KEY = "color";

    private IModuleConfigItem<Integer> color;

    @Override
    public void init(IModule<ModuleColorModulationUnit> module, ModuleConfigItemCreator configItemCreator) {
        color = configItemCreator.createConfigItem(COLOR_CONFIG_KEY, MekanismLang.MODULE_COLOR, ModuleColorData.argb());
    }

    public Color getColor() {
        return Color.argb(color.get());
    }
}