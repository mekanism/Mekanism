package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.world.OreRetrogenFeature;
import mekanism.common.world.ResizableDiskConfig;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class MekanismFeatures {

    private MekanismFeatures() {
    }

    public static final MekanismDeferredRegister<Feature<?>> FEATURES = new MekanismDeferredRegister<>(Registries.FEATURE, Mekanism.MODID);

    public static final MekanismDeferredHolder<Feature<?>, ResizableDiskReplaceFeature> DISK = FEATURES.register("disk", () -> new ResizableDiskReplaceFeature(ResizableDiskConfig.CODEC));
    public static final MekanismDeferredHolder<Feature<?>, ResizableOreFeature> ORE = FEATURES.register("ore", ResizableOreFeature::new);
    public static final MekanismDeferredHolder<Feature<?>, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register("ore_retrogen", OreRetrogenFeature::new);
}