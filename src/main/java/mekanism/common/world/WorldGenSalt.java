package mekanism.common.world;

import java.util.Random;

import mekanism.common.MekanismBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenSalt extends WorldGenerator
{
    private Block blockGen;
    
    private int numberOfBlocks;

    public WorldGenSalt(int blockNum)
    {
        blockGen = MekanismBlocks.SaltBlock;
        numberOfBlocks = blockNum;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos pos)
    {
        if(world.getBlockState(pos).getMaterial() != Material.WATER)
        {
            return false;
        }
        else {
            int toGenerate = random.nextInt(numberOfBlocks - 2) + 2;
            byte yOffset = 1;

            for(int xPos = pos.getX() - toGenerate; xPos <= pos.getX() + toGenerate; xPos++)
            {
                for(int zPos = pos.getZ() - toGenerate; zPos <= pos.getZ() + toGenerate; zPos++)
                {
                    int xOffset = xPos - pos.getX();
                    int zOffset = zPos - pos.getZ();

                    if((xOffset*xOffset) + (zOffset*zOffset) <= toGenerate*toGenerate)
                    {
                        for(int yPos = pos.getY() - yOffset; yPos <= pos.getY() + yOffset; yPos++)
                        {
                            BlockPos newPos = new BlockPos(xPos, yPos, zPos);
                            Block block = world.getBlockState(newPos).getBlock();

                            if(block == Blocks.DIRT || block == Blocks.CLAY || block == MekanismBlocks.SaltBlock)
                            {
                                world.setBlockState(newPos, blockGen.getDefaultState(), 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}