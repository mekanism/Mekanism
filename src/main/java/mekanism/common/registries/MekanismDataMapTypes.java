package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.common.registration.impl.DataMapTypeRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class MekanismDataMapTypes {
    public static final DataMapTypeRegister REGISTER = new DataMapTypeRegister(Mekanism.MODID);

    public static final DataMapType<DamageType, MekaSuitAbsorption> MEKA_SUIT_ABSORPTION = REGISTER.registerSimple(MekaSuitAbsorption.ID.getPath(), Registries.DAMAGE_TYPE, MekaSuitAbsorption.CODEC);
}
