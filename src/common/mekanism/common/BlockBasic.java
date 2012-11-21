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
 * 5: Reinforced Iron
 * 6: Control Panel
 * @author AidanBrady
 *
 */
public class BlockBasic extends Block
{
	public BlockBasic(int i)
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
			case 5:
				return 29;
			case 6:
				return 0;
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
		list.add(new ItemStack(i, 1, 5));
		list.add(new ItemStack(i, 1, 6));
	}
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 2)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, /*1*/ 14, world, x, y, z);
    			return true;
    		}
    	}
    	else if(metadata == 6)
    	{
    		if(!entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, /*1*/ 14, world, x, y, z);
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
    public float getBlockHardness(World world, int x, int y, int z)
    {
		int metadata = world.getBlockMetadata(x, y, z);
		if(metadata == 5)
		{
			return 12F;
		}
        return blockHardness;
    }
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata == 6;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return metadata == 6 ? new TileEntityControlPanel() : null;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
	{
		world.markBlockForRenderUpdate(x, y, z);
		world.updateAllLightTypes(x, y, z);
	}
	
	@Override
	public String getTextureFile()
	{
		return "/resources/mekanism/textures/terrain.png";
	}
}