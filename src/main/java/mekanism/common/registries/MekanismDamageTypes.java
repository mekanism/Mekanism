package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DamageTypeRegister;
import mekanism.common.registration.impl.MekanismDamageType;
import net.minecraft.world.damagesource.DamageEffects;

public class MekanismDamageTypes {

    public static final DamageTypeRegister DAMAGE_TYPES = new DamageTypeRegister(Mekanism.MODID);

    public static final MekanismDamageType FLAMETHROWER = DAMAGE_TYPES.create("flamethrower", 0.1F, DamageEffects.BURNING);
    public static final MekanismDamageType LASER = DAMAGE_TYPES.create("laser", 0.1F);
    public static final MekanismDamageType RADIATION = DAMAGE_TYPES.create("radiation");
    public static final MekanismDamageType SPS = DAMAGE_TYPES.create("sps");
}