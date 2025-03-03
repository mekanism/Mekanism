package mekanism.common.integration.projecte;

import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;
import moze_intel.projecte.api.ProjectERegistries;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;

public class MekanismNormalizedSimpleStacks {

    private MekanismNormalizedSimpleStacks() {
    }

    public static final DeferredMapCodecRegister<NormalizedSimpleStack> NSS_SERIALIZERS = new DeferredMapCodecRegister<>(ProjectERegistries.NSS_SERIALIZER_NAME, Mekanism.MODID);

    public static final DeferredMapCodecHolder<NormalizedSimpleStack, NSSChemical> CHEMICAL = NSS_SERIALIZERS.registerCodec("chemical", () -> NSSChemical.CODEC);
}