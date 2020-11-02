package mekanism.common.world;

import java.util.BitSet;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.util.Constants.BlockFlags;

//Modified copy of vanilla's OreFeature but to support ResizableOreFeatureConfig
public class ResizableOreFeature extends Feature<ResizableOreFeatureConfig> {

    public ResizableOreFeature() {
        super(ResizableOreFeatureConfig.CODEC);
    }

    protected Heightmap.Type getHeightmapType() {
        return Heightmap.Type.OCEAN_FLOOR_WG;
    }

    @Override
    public boolean generate(@Nonnull ISeedReader seedReader, @Nonnull ChunkGenerator chunkGenerator, Random rand, BlockPos pos, ResizableOreFeatureConfig config) {
        float angle = rand.nextFloat() * (float) Math.PI;
        float adjustedSize = config.size.getAsInt() / 8.0F;
        int i = MathHelper.ceil((adjustedSize + 1.0F) / 2.0F);
        float sin = MathHelper.sin(angle) * adjustedSize;
        float cos = MathHelper.cos(angle) * adjustedSize;
        double xMin = pos.getX() + sin;
        double xMax = pos.getX() - sin;
        double zMin = pos.getZ() + cos;
        double zMax = pos.getZ() - cos;
        double yMin = pos.getY() + rand.nextInt(3) - 2;
        double yMax = pos.getY() + rand.nextInt(3) - 2;
        int minXStart = pos.getX() - MathHelper.ceil(adjustedSize) - i;
        int minYStart = pos.getY() - 2 - i;
        int minZStart = pos.getZ() - MathHelper.ceil(adjustedSize) - i;
        int width = 2 * (MathHelper.ceil(adjustedSize) + i);
        int height = 2 * (2 + i);
        for (int l1 = minXStart; l1 <= minXStart + width; ++l1) {
            for (int i2 = minZStart; i2 <= minZStart + width; ++i2) {
                if (minYStart <= seedReader.getHeight(getHeightmapType(), l1, i2)) {
                    return func_207803_a(seedReader, rand, config, xMin, xMax, zMin, zMax, yMin, yMax, minXStart, minYStart, minZStart, width, height);
                }
            }
        }
        return false;
    }

    protected boolean func_207803_a(IWorld world, Random random, ResizableOreFeatureConfig config, double xMin, double xMax, double zMin, double zMax,
          double yMin, double yMax, int minXStart, int minYStart, int minZStart, int width, int height) {
        BitSet bitset = new BitSet(width * height * width);
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int size = config.size.getAsInt();
        double[] adouble = new double[size * 4];
        for (int k = 0; k < size; ++k) {
            float f = k / (float) size;
            int k4 = k * 4;
            adouble[k4] = MathHelper.lerp(f, xMin, xMax);
            adouble[k4 + 1] = MathHelper.lerp(f, yMin, yMax);
            adouble[k4 + 2] = MathHelper.lerp(f, zMin, zMax);
            double d6 = random.nextDouble() * size / 16D;
            adouble[k4 + 3] = ((double) (MathHelper.sin((float) Math.PI * f) + 1) * d6 + 1) / 2D;
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
        for (int j = 0; j < size; ++j) {
            int j4 = j * 4;
            double d1 = adouble[j4 + 3];
            if (d1 >= 0.0D) {
                double d2 = adouble[j4];
                double d3 = adouble[j4 + 1];
                double d4 = adouble[j4 + 2];
                int xStart = Math.max(MathHelper.floor(d2 - d1), minXStart);
                int yStart = Math.max(MathHelper.floor(d3 - d1), minYStart);
                int zStart = Math.max(MathHelper.floor(d4 - d1), minZStart);
                int xEnd = Math.max(MathHelper.floor(d2 + d1), xStart);
                int yEnd = Math.max(MathHelper.floor(d3 + d1), yStart);
                int zEnd = Math.max(MathHelper.floor(d4 + d1), zStart);
                for (int x = xStart; x <= xEnd; ++x) {
                    double d8 = ((double) x + 0.5 - d2) / d1;
                    double d8_squared = d8 * d8;
                    if (d8_squared < 1) {
                        for (int y = yStart; y <= yEnd; ++y) {
                            double d9 = ((double) y + 0.5 - d3) / d1;
                            double d9_squared = d9 * d9;
                            if (d8_squared + d9_squared < 1) {
                                for (int z = zStart; z <= zEnd; ++z) {
                                    double d10 = ((double) z + 0.5 - d4) / d1;
                                    if (d8_squared + d9_squared + d10 * d10 < 1) {
                                        int l2 = x - minXStart + (y - minYStart) * width + (z - minZStart) * width * height;
                                        if (!bitset.get(l2)) {
                                            bitset.set(l2);
                                            mutablePos.setPos(x, y, z);
                                            if (config.target.test(world.getBlockState(mutablePos), random)) {
                                                world.setBlockState(mutablePos, config.state, BlockFlags.BLOCK_UPDATE);
                                                ++i;
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
}