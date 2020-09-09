package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class OreRetrogenFeature extends OreFeature {

    public OreRetrogenFeature(Codec<OreFeatureConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean func_241855_a(@Nonnull ISeedReader seedReader, @Nonnull ChunkGenerator chunkGenerator, Random rand, BlockPos pos, OreFeatureConfig config) {
        float angle = rand.nextFloat() * (float) Math.PI;
        float f1 = config.size / 8.0F;
        int i = MathHelper.ceil((f1 + 1.0F) / 2.0F);
        float sin = MathHelper.sin(angle) * f1;
        float cos = MathHelper.cos(angle) * f1;
        double xStart = pos.getX() + sin;
        double xEnd = pos.getX() - sin;
        double zStart = pos.getZ() + cos;
        double zEnd = pos.getZ() - cos;
        double yStart = pos.getY() + rand.nextInt(3) - 2;
        double yEnd = pos.getY() + rand.nextInt(3) - 2;
        int k = pos.getX() - MathHelper.ceil(f1) - i;
        int l = pos.getY() - 2 - i;
        int i1 = pos.getZ() - MathHelper.ceil(f1) - i;
        int j1 = 2 * (MathHelper.ceil(f1) + i);
        int k1 = 2 * (2 + i);
        for (int l1 = k; l1 <= k + j1; ++l1) {
            for (int i2 = i1; i2 <= i1 + j1; ++i2) {
                //Use OCEAN_FLOOR instead of OCEAN_FLOOR_WG as the chunks are already generated
                if (l <= seedReader.getHeight(Heightmap.Type.OCEAN_FLOOR, l1, i2)) {
                    return this.func_207803_a(seedReader, rand, config, xStart, xEnd, zStart, zEnd, yStart, yEnd, k, l, i1, j1, k1);
                }
            }
        }
        return false;
    }
}