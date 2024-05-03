package mekanism.common.content.gear.mekatool;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;

@ParametersAreNotNullByDefault
public record ModuleTeleportationUnit(boolean requiresBlockTarget) implements ICustomModule<ModuleTeleportationUnit> {

    public static final String REQUIRE_TARGET = "teleportation_requires_block";

    public ModuleTeleportationUnit(IModule<ModuleTeleportationUnit> module) {
        this(module.getBooleanConfigOrFalse(REQUIRE_TARGET));
    }
}