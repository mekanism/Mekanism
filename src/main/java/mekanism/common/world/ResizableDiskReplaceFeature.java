package mekanism.common.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismFeatureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.DiskFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

// Wrapper for vanilla's DiskReplaceFeature to support ResizableDiskConfig.halfHeight (mekanism config IntSupplier)
public record ResizableDiskReplaceFeature(Holder<BlockStateProvider> stateProvider, BlockPredicate target, IntProvider radius, IntSupplier halfHeight) implements Feature {


    public static final MapCodec<ResizableDiskReplaceFeature> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          BlockStateProvider.CODEC.fieldOf(SerializationConstants.STATE_PROVIDER).forGetter(ResizableDiskReplaceFeature::stateProvider),
          BlockPredicate.CODEC.fieldOf(SerializationConstants.TARGET).forGetter(ResizableDiskReplaceFeature::target),
          //TODO - 26.3: Vanilla seems to limit this between 0 and 8, should we follow suit?
          IntProviders.CODEC.fieldOf(SerializationConstants.RADIUS).forGetter(ResizableDiskReplaceFeature::radius)
    ).apply(builder, ResizableDiskReplaceFeature::new));

    public ResizableDiskReplaceFeature(Holder<BlockStateProvider> stateProvider, BlockPredicate target, IntProvider radius) {
        this(stateProvider, target, radius, MekanismConfig.world.salt.halfHeight);
    }

    public DiskFeature asVanillaFeature() {
        return new DiskFeature(stateProvider, target, radius, halfHeight.getAsInt());
    }

    //Note: We don't bother registering this feature as we only use it to avoid having to copy the relevant code while also
    // supporting mods that mixin to change how the disk feature places: https://github.com/mekanism/Mekanism/pull/7968
    //TODO - 26.3: Reimplement support for retrogen
    /*private static final Feature<DiskConfiguration> RETROGEN_DISK = new DiskFeature(DiskConfiguration.CODEC) {
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
    };*/

    @Override
    public MapCodec<? extends Feature> codec() {
        return MekanismFeatureTypes.DISK.get();
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return asVanillaFeature().place(level, chunkGenerator, random, origin);
    }
}
