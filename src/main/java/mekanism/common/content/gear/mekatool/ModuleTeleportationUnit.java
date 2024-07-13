package mekanism.common.content.gear.mekatool;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNotNullByDefault
public record ModuleTeleportationUnit(boolean requiresBlockTarget) implements ICustomModule<ModuleTeleportationUnit> {

    public static final ResourceLocation REQUIRE_TARGET = Mekanism.rl("teleportation_requires_block");

    public ModuleTeleportationUnit(IModule<ModuleTeleportationUnit> module) {
        this(module.getBooleanConfigOrFalse(REQUIRE_TARGET));
    }
}