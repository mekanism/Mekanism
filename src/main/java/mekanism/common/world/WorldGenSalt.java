package mekanism.common.world;

import java.util.Random;

import mekanism.common.MekanismBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
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
    public boolean generate(World world, Random random, int x, int y, int z)
    {
        if(world.getBlock(x, y, z).getMaterial() != Material.water)
        {
            return false;
        }
        else {
            int toGenerate = random.nextInt(numberOfBlocks - 2) + 2;
            byte yOffset = 1;

            for(int xPos = x - toGenerate; xPos <= x + toGenerate; xPos++)
            {
                for(int zPos = z - toGenerate; zPos <= z + toGenerate; zPos++)
                {
                    int xOffset = xPos - x;
                    int zOffset = zPos - z;

                    if((xOffset*xOffset) + (zOffset*zOffset) <= toGenerate*toGenerate)
                    {
                        for(int yPos = y - yOffset; yPos <= y + yOffset; yPos++)
                        {
                            Block block = world.getBlock(xPos, yPos, zPos);

                            if(block == Blocks.dirt || block == Blocks.clay || block == MekanismBlocks.SaltBlock)
                            {
                                world.setBlock(xPos, yPos, zPos, blockGen, 0, 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}