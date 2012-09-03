package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.registry.BlockProxy;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public class BlockTheoreticalElementizer extends BlockMachine
{
    public BlockTheoreticalElementizer(int par1)
    {
        super(par1, "Elementizer.png");
    }
    
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
        if(side == 0 || side == 1)
        {
        	return ObsidianUtils.isActive(world, x, y, z) ? 52 : 50;
        }
        else {
        	if(side == metadata)
        	{
        		return ObsidianUtils.isActive(world, x, y, z) ? currentFrontTextureIndex : 48;
        	}
        	else if(side == ForgeDirection.getOrientation(metadata).getOpposite().ordinal())
        	{
        		return ObsidianUtils.isActive(world, x, y, z) ? currentBackTextureIndex : 49;
        	}
        	else {
        		return ObsidianUtils.isActive(world, x, y, z) ? currentSideTextureIndex : 51;
        	}
        }
    }
    
    public int getBlockTextureFromSide(int side)
    {
    	if(side == 0 || side == 1)
    	{
    		return 50;
    	}
    	else if(side == 3)
    	{
    		return 48;
    	}
    	else {
    		return 51;
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
            TileEntityTheoreticalElementizer tileEntity = (TileEntityTheoreticalElementizer)world.getBlockTileEntity(x, y, z);

            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(ObsidianIngots.instance, 25, world, x, y, z);
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
		return new TileEntityTheoreticalElementizer();
	}
}
