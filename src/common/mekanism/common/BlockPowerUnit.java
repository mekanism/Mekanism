package mekanism.common;

import ic2.api.EnergyNet;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

/**
 * Block class for handling multiple ore block IDs. 
 * 0: Power Unit
 * 1: Advanced Power Unit
 * @author AidanBrady
 *
 */
public class BlockPowerUnit extends BlockContainer
{
	private Random powerRand = new Random();
	
	public BlockPowerUnit(int id)
	{
		super(id, Material.iron);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
		setRequiresSelfNotify();
	}
	
	public int getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		if(meta == 0)
		{
			if(side == 3)
			{
				return 23;
			}
			else {
				return 24;
			}
		}
		else if(meta == 1)
		{
			if(side == 3)
			{
				return 21;
			}
			else {
				return 22;
			}
		}
		return 0;
	}
	
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
    	TileEntityPowerUnit tileEntity = (TileEntityPowerUnit)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;
        
        switch(side)
        {
        	case 0: change = 2; break;
        	case 1: change = 5; break;
        	case 2: change = 3; break;
        	case 3: change = 4; break;
        }
        
        tileEntity.setFacing((short)change);
    }
    
    public int damageDropped(int i)
    {
    	return i;
    }
    
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
	}
	
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityPowerUnit tileEntity = (TileEntityPowerUnit)world.getBlockTileEntity(x, y, z);
        
		if(metadata == 0)
		{
			if(side == tileEntity.facing)
			{
				return 23;
			}
			else {
				return 24;
			}
		}
		else if(metadata == 1)
		{
			if(side == tileEntity.facing)
			{
				return 21;
			}
			else {
				return 22;
			}
		}
		return 0;
    }
	
    public void breakBlock(World world, int par2, int par3, int par4, int i1, int i2)
    {
        TileEntityPowerUnit var5 = (TileEntityPowerUnit)world.getBlockTileEntity(par2, par3, par4);

        if (var5 != null)
        {
            for (int var6 = 0; var6 < var5.getSizeInventory(); ++var6)
            {
                ItemStack var7 = var5.getStackInSlot(var6);

                if (var7 != null)
                {
                    float var8 = powerRand.nextFloat() * 0.8F + 0.1F;
                    float var9 = powerRand.nextFloat() * 0.8F + 0.1F;
                    float var10 = powerRand.nextFloat() * 0.8F + 0.1F;

                    while (var7.stackSize > 0)
                    {
                        int var11 = powerRand.nextInt(21) + 10;

                        if (var11 > var7.stackSize)
                        {
                            var11 = var7.stackSize;
                        }

                        var7.stackSize -= var11;
                        EntityItem var12 = new EntityItem(world, (double)((float)par2 + var8), (double)((float)par3 + var9), (double)((float)par4 + var10), new ItemStack(var7.itemID, var11, var7.getItemDamage()));

                        if (var7.hasTagCompound())
                        {
                            var12.item.setTagCompound((NBTTagCompound)var7.getTagCompound().copy());
                        }

                        float var13 = 0.05F;
                        var12.motionX = (double)((float)powerRand.nextGaussian() * var13);
                        var12.motionY = (double)((float)powerRand.nextGaussian() * var13 + 0.2F);
                        var12.motionZ = (double)((float)powerRand.nextGaussian() * var13);
                        world.spawnEntityInWorld(var12);
                    }
                }
            }
            if(Mekanism.hooks.IC2Loaded)
            {
            	EnergyNet.getForWorld(var5.worldObj).removeTileEntity(var5);
            }
            var5.invalidate();
        }
	        
    	super.breakBlock(world, par2, par3, par4, i1, i2);
    }
    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntityPowerUnit tileEntity = (TileEntityPowerUnit)world.getBlockTileEntity(x, y, z);
        	int metadata = world.getBlockMetadata(x, y, z);
        	
            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(Mekanism.instance, 8, world, x, y, z);
            	}
            	else {
            		return false;
            	}
            }
            return true;
        }
    }
    
    public String getTextureFile()
    {
    	return "/textures/terrain.png";
    }

	public TileEntity createNewTileEntity(World world, int metadata) 
	{
		if(metadata == 0)
		{
			return new TileEntityPowerUnit();
		}
		else if(metadata == 1)
		{
			return new TileEntityAdvancedPowerUnit();
		}
		return null;
	}
	
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
}
