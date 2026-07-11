package mekanism.common.content.gear.shared;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

//Note: While it would be nice to make the Meka-Tool support this, there isn't much point as the only spot on the tool that would be colorable
// is the one that actually represents the fingers from the MekaSuit. Unfortunately we don't have access to that context when rendering the
// item variant so for now we just don't handle it
public record ModuleColorModulationUnit(int color) implements ICustomModule<ModuleColorModulationUnit> {

    public static final Identifier COLOR = Mekanism.rl("color");

    public ModuleColorModulationUnit(IModule<ModuleColorModulationUnit> module) {
        this(module.<Integer>getConfigOrThrow(COLOR).get());
    }
}