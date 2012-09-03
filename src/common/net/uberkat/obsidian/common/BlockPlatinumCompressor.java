package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;

public class BlockPlatinumCompressor extends BlockMachine
{
    public BlockPlatinumCompressor(int par1)
    {
        super(par1, "Compressor.png");
    }

    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        
        if(side == metadata)
        {
        	return ObsidianUtils.isActive(world, x, y, z) ? currentFrontTextureIndex : 17;
        }
        else {
        	return 16;
        }
    }
    
    public int getBlockTextureFromSide(int side)
    {
    	if(side == 3)
    	{
    		return 17;
    	}
    	else {
    		return 16;
    	}
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
            TileEntityPlatinumCompressor tileEntity = (TileEntityPlatinumCompressor)world.getBlockTileEntity(x, y, z);

            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(ObsidianIngots.instance, 22, world, x, y, z);
            	}
            	else {
            		return false;
            	}
            }

            return true;
        }
    }

    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityPlatinumCompressor();
    }
}
