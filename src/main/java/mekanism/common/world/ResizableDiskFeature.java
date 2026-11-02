package mekanism.common.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.IntSupplier;
import mekanism.api.SerializationConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismFeatureTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.DiskFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

// Wrapper for vanilla's DiskFeature to support ResizableDiskConfig.halfHeight (mekanism config IntSupplier)
public record ResizableDiskFeature(Holder<BlockStateProvider> stateProvider, BlockPredicate target, IntProvider radius, IntSupplier halfHeight) implements Feature {

    public static final MapCodec<ResizableDiskFeature> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          BlockStateProvider.CODEC.fieldOf(SerializationConstants.STATE_PROVIDER).forGetter(ResizableDiskFeature::stateProvider),
          BlockPredicate.CODEC.fieldOf(SerializationConstants.TARGET).forGetter(ResizableDiskFeature::target),
          //Note: We apply vanilla's range requirements as part of the config instead of here so that it doesn't fail when trying to deserialize
          IntProviders.CODEC.fieldOf(SerializationConstants.RADIUS).forGetter(ResizableDiskFeature::radius)
    ).apply(builder, ResizableDiskFeature::new));

    public ResizableDiskFeature(Holder<BlockStateProvider> stateProvider, BlockPredicate target, IntProvider radius) {
        this(stateProvider, target, radius, MekanismConfig.world.salt.halfHeight);
    }

    public DiskFeature asVanillaFeature() {
        return new DiskFeature(stateProvider, target, radius, halfHeight.getAsInt());
    }

    @Override
    public MapCodec<? extends Feature> codec() {
        return MekanismFeatureTypes.DISK.get();
    }

    /// Copy of vanilla [DiskFeature]
    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (level instanceof WorldGenRegion) {
            //Place as vanilla as we aren't retrogening so we want to provide as best support for mods changing the disk feature as possible
            // See: https://github.com/mekanism/Mekanism/pull/7968
            return asVanillaFeature().place(level, chunkGenerator, random, origin);
        }
        boolean placedAny = false;
        int originY = origin.getY();
        int halfHeight = this.halfHeight.getAsInt();
        int top = originY + halfHeight;
        int bottom = originY - halfHeight - 1;
        int r = this.radius.sample(random);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (BlockPos columnPos : BlockPos.betweenClosed(origin.offset(-r, 0, -r), origin.offset(r, 0, r))) {
            int xd = columnPos.getX() - origin.getX();
            int zd = columnPos.getZ() - origin.getZ();
            if (xd * xd + zd * zd <= r * r) {
                placedAny |= this.placeColumn(level, random, top, bottom, mutablePos.set(columnPos));
            }
        }

        return placedAny;
    }

    /// Copy of vanilla [DiskFeature] but without the call to markAboveForPostProcesssing as existing chunks don't support that.
    private boolean placeColumn(WorldGenLevel level, RandomSource random, int top, int bottom, BlockPos.MutableBlockPos pos) {
        boolean placedAny = false;
        for (int y = top; y > bottom; y--) {
            pos.setY(y);
            if (this.target.test(level, pos)) {
                BlockState state = this.stateProvider.value().getOptionalState(level, random, pos);
                if (state != null) {
                    level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }
}
