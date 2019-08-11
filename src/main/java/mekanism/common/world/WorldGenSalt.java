package mekanism.common.world;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.MekanismBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class WorldGenSalt extends Feature {

    private Block blockGen;

    private int numberOfBlocks;

    public WorldGenSalt(int blockNum) {
        blockGen = MekanismBlock.SALT_BLOCK.getBlock();
        numberOfBlocks = blockNum;
    }

    @Override
    public boolean place(@Nonnull IWorld world, @Nonnull ChunkGenerator generator, @Nonnull Random random, @Nonnull BlockPos pos, @Nonnull IFeatureConfig config) {
        if (world.getBlockState(pos).getMaterial() != Material.WATER) {
            return false;
        }
        int toGenerate = random.nextInt(numberOfBlocks - 2) + 2;
        byte yOffset = 1;
        for (int xPos = pos.getX() - toGenerate; xPos <= pos.getX() + toGenerate; xPos++) {
            for (int zPos = pos.getZ() - toGenerate; zPos <= pos.getZ() + toGenerate; zPos++) {
                int xOffset = xPos - pos.getX();
                int zOffset = zPos - pos.getZ();
                if ((xOffset * xOffset) + (zOffset * zOffset) <= toGenerate * toGenerate) {
                    for (int yPos = pos.getY() - yOffset; yPos <= pos.getY() + yOffset; yPos++) {
                        BlockPos newPos = new BlockPos(xPos, yPos, zPos);
                        Block block = world.getBlockState(newPos).getBlock();
                        if (block == Blocks.DIRT || block == Blocks.CLAY || block == blockGen) {
                            world.setBlockState(newPos, blockGen.getDefaultState(), 2);
                        }
                    }
                }
            }
        }
        return true;
    }
}