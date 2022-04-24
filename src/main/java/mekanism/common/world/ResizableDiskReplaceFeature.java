package mekanism.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

//Modified copy of vanilla's DiskReplaceFeature/BaseDiskFeature but to support ResizableSphereReplaceConfig
// and without support for falling blocks
public class ResizableDiskReplaceFeature extends Feature<ResizableDiskConfig> {

    public ResizableDiskReplaceFeature(Codec<ResizableDiskConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResizableDiskConfig> context) {
        boolean placed = false;
        BlockPos pos = context.origin();
        WorldGenLevel world = context.level();
        if (world.getFluidState(pos).is(FluidTags.WATER)) {
            ResizableDiskConfig config = context.config();
            int halfHeight = config.halfHeight.getAsInt();
            int yMax = pos.getY() + halfHeight;
            int yMin = pos.getY() - halfHeight - 1;
            int radius = config.radius.sample(context.random());
            int radiusSquared = radius * radius;
            for (int x = pos.getX() - radius; x <= pos.getX() + radius; ++x) {
                int xRadius = x - pos.getX();
                int xRadiusSquared = xRadius * xRadius;
                for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; ++z) {
                    int zRadius = z - pos.getZ();
                    if (xRadiusSquared + zRadius * zRadius <= radiusSquared) {
                        for (int y = yMax; y > yMin; --y) {
                            BlockPos targetPos = new BlockPos(x, y, z);
                            Block blockAtTarget = world.getBlockState(targetPos).getBlock();
                            for (BlockState target : config.targets) {
                                if (target.is(blockAtTarget)) {
                                    world.setBlock(targetPos, config.state, Block.UPDATE_CLIENTS);
                                    markAboveForPostProcessing(world, targetPos);
                                    placed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }
}
