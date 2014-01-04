package codechicken.multipart.minecraft;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.IRandomDisplayTick;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;

public class TorchPart extends McSidedMetaPart implements IRandomDisplayTick
{
    public static BlockTorch torch = (BlockTorch) Block.torchWood;
    public static int[] metaSideMap = new int[]{-1, 4, 5, 2, 3, 0};
    public static int[] sideMetaMap = new int[]{5, 0, 3, 4, 1, 2};
    
    public TorchPart()
    {
    }
    
    public TorchPart(int meta)
    {
        super(meta);
    }
    
    @Override
    public Block getBlock()
    {
        return torch;
    }
    
    @Override
    public String getType()
    {
        return "mc_torch";
    }
    
    @Override
    public Cuboid6 getBounds()
    {
        return getBounds(meta);
    }
    
    public Cuboid6 getBounds(int meta)
    {
        double d = 0.15;
        if (meta == 1)
            return new Cuboid6(0, 0.2, 0.5 - d, d * 2, 0.8, 0.5 + d);
        if (meta == 2)
            return new Cuboid6(1 - d * 2, 0.2, 0.5 - d, 1, 0.8, 0.5 + d);
        if (meta == 3)
            return new Cuboid6(0.5 - d, 0.2, 0, 0.5 + d, 0.8, d * 2);
        if (meta == 4)
            return new Cuboid6(0.5 - d, 0.2, 1 - d * 2, 0.5 + d, 0.8, 1);
        
        d = 0.1;
        return new Cuboid6(0.5 - d, 0, 0.5 - d, 0.5 + d, 0.6, 0.5 + d);
    }
    
    @Override
    public int sideForMeta(int meta)
    {
        return metaSideMap[meta];
    }
    
    @Override
    public boolean canStay()
    {
        if(sideForMeta(meta) == 0)
        {
            Block block = Block.blocksList[world().getBlockId(x(), y()-1, z())];
            if(block != null && block.canPlaceTorchOnTop(world(), x(), y()-1, z()))
                return true;
        }
        return super.canStay();
    }
    
    public static McBlockPart placement(World world, BlockCoord pos, int side)
    {
        if(side == 0)
            return null;
        pos = pos.copy().offset(side^1);
        if(!world.isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side)))
        {
            if(side == 1)
            {
                Block block = Block.blocksList[world.getBlockId(pos.x, pos.y, pos.z)];
                if(block == null || !block.canPlaceTorchOnTop(world, pos.x, pos.y, pos.z))
                    return null;
            }
            else
            {
                return null;
            }
        }
        
        return new TorchPart(sideMetaMap[side^1]);
    }

    @Override
    public void randomDisplayTick(Random random)
    {
        double d0 = x() + 0.5;
        double d1 = y() + 0.7;
        double d2 = z() + 0.5;
        double d3 = 0.22D;
        double d4 = 0.27D;
        
        World world = world();
        if (meta == 1)
        {
            world.spawnParticle("smoke", d0 - d4, d1 + d3, d2, 0, 0, 0);
            world.spawnParticle("flame", d0 - d4, d1 + d3, d2, 0, 0, 0);
        }
        else if (meta == 2)
        {
            world.spawnParticle("smoke", d0 + d4, d1 + d3, d2, 0, 0, 0);
            world.spawnParticle("flame", d0 + d4, d1 + d3, d2, 0, 0, 0);
        }
        else if (meta == 3)
        {
            world.spawnParticle("smoke", d0, d1 + d3, d2 - d4, 0, 0, 0);
            world.spawnParticle("flame", d0, d1 + d3, d2 - d4, 0, 0, 0);
        }
        else if (meta == 4)
        {
            world.spawnParticle("smoke", d0, d1 + d3, d2 + d4, 0, 0, 0);
            world.spawnParticle("flame", d0, d1 + d3, d2 + d4, 0, 0, 0);
        }
        else
        {
            world.spawnParticle("smoke", d0, d1, d2, 0, 0, 0);
            world.spawnParticle("flame", d0, d1, d2, 0, 0, 0);
        }
    }
}
