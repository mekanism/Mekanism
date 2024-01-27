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

    public static final MekanismDeferredHolder<NSSCodecHolder<?>, NSSCodecHolder<NSSGas>> GAS = NSS_SERIALIZERS.register("gas", () -> NSSGas.CODECS);
    public static final MekanismDeferredHolder<NSSCodecHolder<?>, NSSCodecHolder<NSSInfuseType>> INFUSE_TYPE = NSS_SERIALIZERS.register("infuse_type", () -> NSSInfuseType.CODECS);
    public static final MekanismDeferredHolder<NSSCodecHolder<?>, NSSCodecHolder<NSSPigment>> PIGMENT = NSS_SERIALIZERS.register("pigment", () -> NSSPigment.CODECS);
    public static final MekanismDeferredHolder<NSSCodecHolder<?>, NSSCodecHolder<NSSSlurry>> SLURRY = NSS_SERIALIZERS.register("slurry", () -> NSSSlurry.CODECS);
}