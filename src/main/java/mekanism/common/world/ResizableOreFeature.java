package mekanism.common.world;

import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import org.jetbrains.annotations.NotNull;

//Modified copy of vanilla's OreFeature but to support ResizableOreFeatureConfig
public class ResizableOreFeature extends Feature<ResizableOreFeatureConfig> {

    public ResizableOreFeature() {
        super(ResizableOreFeatureConfig.CODEC);
    }

    protected Heightmap.Types getHeightmapType() {
        return Heightmap.Types.OCEAN_FLOOR_WG;
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<ResizableOreFeatureConfig> context) {
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        WorldGenLevel world = context.level();
        ResizableOreFeatureConfig config = context.config();
        float angle = random.nextFloat() * Mth.PI;
        float adjustedSize = config.size().getAsInt() / 8.0F;
        int i = Mth.ceil((adjustedSize + 1.0F) / 2.0F);
        double sin = Math.sin(angle) * adjustedSize;
        double cos = Math.cos(angle) * adjustedSize;
        double xMin = pos.getX() + sin;
        double xMax = pos.getX() - sin;
        double zMin = pos.getZ() + cos;
        double zMax = pos.getZ() - cos;
        double yMin = pos.getY() + random.nextInt(3) - 2;
        double yMax = pos.getY() + random.nextInt(3) - 2;
        int minXStart = pos.getX() - Mth.ceil(adjustedSize) - i;
        int minYStart = pos.getY() - 2 - i;
        int minZStart = pos.getZ() - Mth.ceil(adjustedSize) - i;
        int width = 2 * (Mth.ceil(adjustedSize) + i);
        int height = 2 * (2 + i);
        for (int x = minXStart; x <= minXStart + width; ++x) {
            for (int z = minZStart; z <= minZStart + width; ++z) {
                if (minYStart <= world.getHeight(getHeightmapType(), x, z)) {
                    return doPlace(world, random, config, xMin, xMax, zMin, zMax, yMin, yMax, minXStart, minYStart, minZStart, width, height);
                }
            }
        }
        return false;
    }

    protected boolean doPlace(WorldGenLevel world, RandomSource random, ResizableOreFeatureConfig config, double xMin, double xMax, double zMin, double zMax, double yMin,
          double yMax, int minXStart, int minYStart, int minZStart, int width, int height) {
        BitSet bitset = new BitSet(width * height * width);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int size = config.size().getAsInt();
        double[] adouble = new double[size * 4];
        for (int k = 0; k < size; ++k) {
            float f = k / (float) size;
            int k4 = k * 4;
            adouble[k4] = Mth.lerp(f, xMin, xMax);
            adouble[k4 + 1] = Mth.lerp(f, yMin, yMax);
            adouble[k4 + 2] = Mth.lerp(f, zMin, zMax);
            double d3 = random.nextDouble() * size / 16D;
            adouble[k4 + 3] = ((Mth.sin(Mth.PI * f) + 1) * d3 + 1) / 2D;
        }
        for (int i = 0; i < size - 1; ++i) {
            int i4 = i * 4;
            if (adouble[i4 + 3] > 0) {
                for (int j = i + 1; j < size; ++j) {
                    int j4 = j * 4;
                    if (adouble[j4 + 3] > 0) {
                        double d1 = adouble[i4] - adouble[j4];
                        double d2 = adouble[i4 + 1] - adouble[j4 + 1];
                        double d3 = adouble[i4 + 2] - adouble[j4 + 2];
                        double d4 = adouble[i4 + 3] - adouble[j4 + 3];
                        if (d4 * d4 > d1 * d1 + d2 * d2 + d3 * d3) {
                            if (d4 > 0) {
                                adouble[j4 + 3] = -1;
                            } else {
                                adouble[i4 + 3] = -1;
                            }
                        }
                    }
                }
            }
        }
        int i = 0;
        try (BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(world)) {
            float discardChanceOnAirExposure = config.discardChanceOnAirExposure().getAsFloat();
            for (int j = 0; j < size; ++j) {
                int j4 = j * 4;
                double d1 = adouble[j4 + 3];
                if (d1 >= 0) {
                    double d2 = adouble[j4];
                    double d3 = adouble[j4 + 1];
                    double d4 = adouble[j4 + 2];
                    int xStart = Math.max(Mth.floor(d2 - d1), minXStart);
                    int yStart = Math.max(Mth.floor(d3 - d1), minYStart);
                    int zStart = Math.max(Mth.floor(d4 - d1), minZStart);
                    int xEnd = Math.max(Mth.floor(d2 + d1), xStart);
                    int yEnd = Math.max(Mth.floor(d3 + d1), yStart);
                    int zEnd = Math.max(Mth.floor(d4 + d1), zStart);
                    for (int x = xStart; x <= xEnd; ++x) {
                        double d5 = (x + 0.5D - d2) / d1;
                        double d5_squared = d5 * d5;
                        if (d5_squared < 1) {
                            for (int y = yStart; y <= yEnd; ++y) {
                                double d6 = (y + 0.5D - d3) / d1;
                                double d6_squared = d6 * d6;
                                if (d5_squared + d6_squared < 1) {
                                    for (int z = zStart; z <= zEnd; ++z) {
                                        double d7 = (z + 0.5D - d4) / d1;
                                        if (d5_squared + d6_squared + d7 * d7 < 1.0D && !world.isOutsideBuildHeight(y)) {
                                            int l2 = x - minXStart + (y - minYStart) * width + (z - minZStart) * width * height;
                                            if (!bitset.get(l2)) {
                                                bitset.set(l2);
                                                mutablePos.set(x, y, z);
                                                if (world.ensureCanWrite(mutablePos)) {
                                                    LevelChunkSection section = bulkSectionAccess.getSection(mutablePos);
                                                    if (section != null) {
                                                        int sectionX = SectionPos.sectionRelative(x);
                                                        int sectionY = SectionPos.sectionRelative(y);
                                                        int sectionZ = SectionPos.sectionRelative(z);
                                                        BlockState state = section.getBlockState(sectionX, sectionY, sectionZ);
                                                        for (TargetBlockState targetState : config.targetStates()) {
                                                            if (canPlaceOre(state, bulkSectionAccess::getBlockState, random, discardChanceOnAirExposure, targetState, mutablePos)) {
                                                                section.setBlockState(sectionX, sectionY, sectionZ, targetState.state, false);
                                                                ++i;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return i > 0;
    }

    private static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> adjacentStateAccessor, RandomSource random, float discardChanceOnAirExposure,
          TargetBlockState targetState, BlockPos.MutableBlockPos mutablePos) {
        if (!targetState.target.test(state, random)) {
            return false;
        } else if (shouldSkipAirCheck(random, discardChanceOnAirExposure)) {
            return true;
        } else {
            return !isAdjacentToAir(adjacentStateAccessor, mutablePos);
        }
    }

    private static boolean shouldSkipAirCheck(RandomSource random, float discardChanceOnAirExposure) {
        if (discardChanceOnAirExposure <= 0.0F) {
            return true;
        } else if (discardChanceOnAirExposure >= 1.0F) {
            return false;
        }
        return random.nextFloat() >= discardChanceOnAirExposure;
    }
}