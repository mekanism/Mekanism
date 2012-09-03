package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;

public class BlockCrusher extends BlockMachine
{
    public BlockCrusher(int par1)
    {
        super(par1, "terrain.png");
    }

    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
        int metadata = world.getBlockMetadata(x, y, z);
        
        if(side == metadata)
        {
        	return ObsidianUtils.isActive(world, x, y, z) ? 16 : 17;
        }
        else {
        	return 2;
        }
    }
    
    public int getBlockTextureFromSide(int side)
    {
    	if(side == 3)
    	{
    		return 17;
    	}
    	else {
    		return 2;
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
            TileEntityCrusher tileEntity = (TileEntityCrusher)world.getBlockTileEntity(x, y, z);

            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(ObsidianIngots.instance, 24, world, x, y, z);
            	}
            	else {
            		return false;
            	}
            }

            return true;
        }
    }
    
    public TileEntity createNewTileEntity(World var1) 
    {
    	return new TileEntityCrusher();
    }
}
