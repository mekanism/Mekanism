package mekanism.generators.common.registries;

import mekanism.common.registration.impl.DamageTypeRegister;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorsDamageTypes {

    public static final DamageTypeRegister DAMAGE_TYPES = new DamageTypeRegister(MekanismGenerators.MODID);

    public static final MekanismDamageType FUSION = DAMAGE_TYPES.create("fusion");
}