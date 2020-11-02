package mekanism.common.world;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

//Modified copy of vanilla's SphereReplaceFeature/AbstractSphereReplaceConfig but to support ResizableSphereReplaceConfig
public class ResizableSphereReplaceFeature extends Feature<ResizableSphereReplaceConfig> {

    public ResizableSphereReplaceFeature(Codec<ResizableSphereReplaceConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(@Nonnull ISeedReader reader, @Nonnull ChunkGenerator generator, @Nonnull Random rand, @Nonnull BlockPos pos,
          @Nonnull ResizableSphereReplaceConfig config) {
        boolean flag = false;
        if (reader.getFluidState(pos).isTagged(FluidTags.WATER)) {
            int radius = config.getRadius(rand);
            int ySize = config.ySize.getAsInt();
            for (int j = pos.getX() - radius; j <= pos.getX() + radius; ++j) {
                for (int k = pos.getZ() - radius; k <= pos.getZ() + radius; ++k) {
                    int l = j - pos.getX();
                    int i1 = k - pos.getZ();
                    if (l * l + i1 * i1 <= radius * radius) {
                        for (int j1 = pos.getY() - ySize; j1 <= pos.getY() + ySize; ++j1) {
                            BlockPos blockpos = new BlockPos(j, j1, k);
                            Block block = reader.getBlockState(blockpos).getBlock();
                            for (BlockState blockstate : config.targets) {
                                if (blockstate.isIn(block)) {
                                    reader.setBlockState(blockpos, config.state, 2);
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return flag;
    }
}
