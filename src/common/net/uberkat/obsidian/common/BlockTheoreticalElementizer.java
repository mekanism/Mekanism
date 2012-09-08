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
        super(par1);
    }
    
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	TileEntityMachine tileEntity = (TileEntityMachine)world.getBlockTileEntity(x, y, z);
    	
        if(side == 0 || side == 1)
        {
        	return isActive(world, x, y, z) ? 20 : 18;
        }
        else {
        	if(side == tileEntity.facing)
        	{
        		return isActive(world, x, y, z) ? tileEntity.textureIndex + 64 : 16;
        	}
        	else if(side == ForgeDirection.getOrientation(tileEntity.facing).getOpposite().ordinal())
        	{
        		return isActive(world, x, y, z) ? tileEntity.textureIndex + 80 : 17;
        	}
        	else {
        		return isActive(world, x, y, z) ? tileEntity.textureIndex + 96 : 19;
        	}
        }
    }
    
    public int getBlockTextureFromSide(int side)
    {
    	if(side == 0 || side == 1)
    	{
    		return 18;
    	}
    	else if(side == 3)
    	{
    		return 16;
    	}
    	else {
    		return 19;
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
