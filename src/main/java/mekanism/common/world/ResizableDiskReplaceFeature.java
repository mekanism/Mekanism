package mekanism.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

// Wrapper for vanilla's DiskReplaceFeature to support ResizableDiskConfig.halfHeight (mekanism config IntSupplier)
public class ResizableDiskReplaceFeature extends Feature<ResizableDiskConfig> {

    public ResizableDiskReplaceFeature(Codec<ResizableDiskConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResizableDiskConfig> context) {
        ResizableDiskConfig config = context.config();
        DiskConfiguration vanillaConfig = new DiskConfiguration(
                config.stateProvider,
                config.target,
                config.radius,
                config.halfHeight.getAsInt()
        );
        FeaturePlaceContext<DiskConfiguration> vanillaContext = new FeaturePlaceContext<>(
                context.topFeature(),
                context.level(),
                context.chunkGenerator(),
                context.random(),
                context.origin(),
                vanillaConfig
        );
        return Feature.DISK.place(vanillaContext);
    }
}
