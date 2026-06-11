package mekanism.common.content.gear.mekatool;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public record ModuleTeleportationUnit(boolean requiresBlockTarget) implements ICustomModule<ModuleTeleportationUnit> {

    public static final Identifier REQUIRE_TARGET = Mekanism.rl("teleportation_requires_block");

    public ModuleTeleportationUnit(IModule<ModuleTeleportationUnit> module) {
        this(module.getBooleanConfigOrFalse(REQUIRE_TARGET));
    }
}