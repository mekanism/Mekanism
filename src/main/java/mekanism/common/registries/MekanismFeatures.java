package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.world.OreRetrogenFeature;
import mekanism.common.world.ResizableDiskConfig;
import mekanism.common.world.ResizableDiskReplaceFeature;
import mekanism.common.world.ResizableOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismFeatures {

    private MekanismFeatures() {
    }

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Mekanism.MODID);

    public static final DeferredHolder<Feature<?>, ResizableDiskReplaceFeature> DISK = FEATURES.register("disk", () -> new ResizableDiskReplaceFeature(ResizableDiskConfig.CODEC));
    public static final DeferredHolder<Feature<?>, ResizableOreFeature> ORE = FEATURES.register("ore", ResizableOreFeature::new);
    public static final DeferredHolder<Feature<?>, OreRetrogenFeature> ORE_RETROGEN = FEATURES.register("ore_retrogen", OreRetrogenFeature::new);
}