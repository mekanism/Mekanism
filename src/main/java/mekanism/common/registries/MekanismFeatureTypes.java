package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class MekanismFeatureTypes {

    private MekanismFeatureTypes() {
    }

    public static final DeferredMapCodecRegister<Feature> FEATURE_TYPES = new DeferredMapCodecRegister<>(Registries.FEATURE_TYPE, Mekanism.MODID);

    public static final DeferredMapCodecHolder<Feature, ResizableDiskReplaceFeature> DISK = FEATURE_TYPES.registerCodec("disk", () -> ResizableDiskReplaceFeature.CODEC);
    public static final DeferredMapCodecHolder<Feature, ResizableOreFeature> ORE = FEATURE_TYPES.registerCodec("ore", () -> ResizableOreFeature.CODEC);
}