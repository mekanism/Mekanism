package mekanism.common.integration.projecte;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import moze_intel.projecte.api.ProjectERegistries;
import moze_intel.projecte.api.codec.NSSCodecHolder;

public class MekanismNormalizedSimpleStacks {

    private MekanismNormalizedSimpleStacks() {
    }

    public static final MekanismDeferredRegister<NSSCodecHolder<?>> NSS_SERIALIZERS = new MekanismDeferredRegister<>(ProjectERegistries.NSS_SERIALIZER_NAME, Mekanism.MODID);

    public static final MekanismDeferredHolder<NSSCodecHolder<?>, NSSCodecHolder<NSSChemical>> CHEMICAL = NSS_SERIALIZERS.register("chemical", () -> NSSChemical.CODECS);
}