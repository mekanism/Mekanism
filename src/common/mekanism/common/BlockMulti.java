package mekanism.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import mekanism.client.GuiControlPanel;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeChunkManager;

/**
 * Block class for handling multiple metal block IDs.
 * 0: Platinum Block
 * 1: Redstone Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * @author AidanBrady
 *
 */
public class BlockMulti extends Block
{
	public BlockMulti(int i)
	{
		super(i, Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
		setRequiresSelfNotify();
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		switch(meta)
		{
			case 0:
				return 2;
			case 1:
				return 1;
			case 2:
				return 0;
			case 3:
				return 10;
			case 4:
				return 11;
		}
		return 0;
	}
	
	@Override
	public int damageDropped(int i)
	{
		return i;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
		list.add(new ItemStack(i, 1, 2));
		list.add(new ItemStack(i, 1, 3));
		list.add(new ItemStack(i, 1, 4));
	}
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 2)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 1, world, x, y, z);
    			//entityplayer.openGui(Mekanism.instance, 10, world, x, y, z);
    			return true;
    		}
    	}
        return false;
    }
    
	@Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) 
    {
        int metadata = world.getBlockMetadata(x, y, z);
        switch(metadata)
        {
        	case 2:
        		return 8;
        	case 4:
        		return 15;
        	case 5:
        		return 15;
        }
        return 0;
    }
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		if (world.isBlockIndirectlyGettingPowered(x, y + 1, z) && world.getBlockMetadata(z, y, z) == 5)
		{
			Chunk chunkInside = world.getChunkFromBlockCoords(x, z);
			int xPosMax = chunkInside.xPosition << 4;
			int zPosMax = chunkInside.zPosition << 4;
			int xPosMin = xPosMax + 16;
			int zPosMin = zPosMax + 16;
			
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 1.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 2.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 3.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 4.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMax + 1.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMax + 2.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMax + 3.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMax + 4.5, 0, 0, 0);
			
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 1.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 2.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 3.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 4.5, y + 1, zPosMax + 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMax + 1.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMax + 2.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMax + 3.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMax + 4.5, 0, 0, 0);
			
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 1.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 2.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 3.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 4.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMin - 1.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMin - 2.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMin - 3.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMax + 0.5, y + 1, zPosMin - 4.5, 0, 0, 0);
			
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 1.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 2.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 3.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 4.5, y + 1, zPosMin - 0.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMin - 1.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMin - 2.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMin - 3.5, 0, 0, 0);
			world.spawnParticle("portal", xPosMin - 0.5, y + 1, zPosMin - 4.5, 0, 0, 0);
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
	{
		world.markBlockAsNeedsUpdate(x, y, z);
		world.updateAllLightTypes(x, y, z);
	}
	
	@Override
	public String getTextureFile()
	{
		return "/resources/mekanism/textures/terrain.png";
	}
}