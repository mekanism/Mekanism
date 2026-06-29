package mekanism.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.DiskFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

// Wrapper for vanilla's DiskReplaceFeature to support ResizableDiskConfig.halfHeight (mekanism config IntSupplier)
public class ResizableDiskReplaceFeature extends Feature<ResizableDiskConfig> {

    //Note: We don't bother registering this feature as we only use it to avoid having to copy the relevant code while also
    // supporting mods that mixin to change how the disk feature places: https://github.com/mekanism/Mekanism/pull/7968
    private static final Feature<DiskConfiguration> RETROGEN_DISK = new DiskFeature(DiskConfiguration.CODEC) {
        @Override
        protected void markAboveForPostProcessing(WorldGenLevel level, BlockPos placePos) {
            BlockPos.MutableBlockPos pos = placePos.mutable();
            for (int i = 0; i < 2; i++) {
                pos.move(Direction.UP);
                if (level.getBlockState(pos).isAir()) {
                    return;
                }
                ChunkAccess chunk = level.getChunk(pos);
                if (!(chunk instanceof LevelChunk)) {
                    //If this chunk already exists, don't bother marking it for post-processing, as existing chunks don't support that
                    chunk.markPosForPostProcessing(pos);
                }
            }
        }
    };

    public ResizableDiskReplaceFeature(Codec<ResizableDiskConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResizableDiskConfig> context) {
        FeaturePlaceContext<DiskConfiguration> vanillaContext = new FeaturePlaceContext<>(
              context.topFeature(),
              context.level(),
              context.chunkGenerator(),
              context.random(),
              context.origin(),
              context.config().asVanillaConfig()
        );
        return RETROGEN_DISK.place(vanillaContext);
    }
}
