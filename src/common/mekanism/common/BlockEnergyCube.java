package mekanism.common;

import ic2.api.EnergyNet;

import java.util.List;
import java.util.Random;

import universalelectricity.core.implement.IItemElectric;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import mekanism.api.IEnergyCube;
import mekanism.api.IEnergyCube.EnumTier;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

/**
 * Block class for handling multiple energy cube block IDs. 
 * 0: Power Unit
 * 1: Advanced Power Unit
 * 2: Ultimate Power Unit
 * @author AidanBrady
 *
 */
public class BlockEnergyCube extends BlockContainer
{
	private Random powerRand = new Random();
	
	public BlockEnergyCube(int id)
	{
		super(id, Material.iron);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public void setBlockBoundsForItemRender()
	{
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public int getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		if(meta == 0)
		{
			if(side == 3)
			{
				return 4;
			}
			else {
				return 21;
			}
		}
		else if(meta == 1)
		{
			if(side == 3)
			{
				return 38;
			}
			else {
				return 22;
			}
		}
		else if(meta == 2)
		{
			if(side == 3)
			{
				return 24;
			}
			else {
				return 23;
			}
		}
		return 0;
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int height = Math.round(entityliving.rotationPitch);
        int change = 3;
        
        if(height >= 65)
        {
        	change = 1;
        }
        else if(height <= -65)
        {
        	change = 0;
        }
        else {
	        switch(side)
	        {
	        	case 0: change = 2; break;
	        	case 1: change = 5; break;
	        	case 2: change = 3; break;
	        	case 3: change = 4; break;
	        }
        }
        
        tileEntity.setFacing((short)change);
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
        
		if(tileEntity.tier == EnumTier.BASIC)
		{
			if(side == tileEntity.facing)
			{
				return 4;
			}
			else {
				return 21;
			}
		}
		else if(tileEntity.tier == EnumTier.ADVANCED)
		{
			if(side == tileEntity.facing)
			{
				return 38;
			}
			else {
				return 22;
			}
		}
		else if(tileEntity.tier == EnumTier.ULTIMATE)
		{
			if(side == tileEntity.facing)
			{
				return 24;
			}
			else {
				return 23;
			}
		}
		return 0;
    }
	
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
        TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);

        if (tileEntity != null)
        {
        	for (int i = 0; i < tileEntity.getSizeInventory(); ++i)
            {
                ItemStack slotStack = tileEntity.getStackInSlot(i);

                if (slotStack != null)
                {
                    float xRandom = powerRand.nextFloat() * 0.8F + 0.1F;
                    float yRandom = powerRand.nextFloat() * 0.8F + 0.1F;
                    float zRandom = powerRand.nextFloat() * 0.8F + 0.1F;

                    while (slotStack.stackSize > 0)
                    {
                        int j = powerRand.nextInt(21) + 10;

                        if (j > slotStack.stackSize)
                        {
                            j = slotStack.stackSize;
                        }

                        slotStack.stackSize -= j;
                        EntityItem item = new EntityItem(world, (double)((float)x + xRandom), (double)((float)y + yRandom), (double)((float)z + zRandom), new ItemStack(slotStack.itemID, j, slotStack.getItemDamage()));

                        if (slotStack.hasTagCompound())
                        {
                            item.item.setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
                        }

                        float k = 0.05F;
                        item.motionX = (double)((float)powerRand.nextGaussian() * k);
                        item.motionY = (double)((float)powerRand.nextGaussian() * k + 0.2F);
                        item.motionZ = (double)((float)powerRand.nextGaussian() * k);
                        world.spawnEntityInWorld(item);
                    }
                }
            }
        	
            EntityItem entityItem = new EntityItem(world, x, y, z, new ItemStack(Mekanism.EnergyCube));
            
            float motion = 0.05F;
            entityItem.motionX = powerRand.nextGaussian() * motion;
            entityItem.motionY = powerRand.nextGaussian() * motion + 0.2F;
            entityItem.motionZ = powerRand.nextGaussian() * motion;
            
            IEnergyCube energyCube = (IEnergyCube)entityItem.item.getItem();
            energyCube.setTier(entityItem.item, tileEntity.tier);
            
            IItemElectric electricItem = (IItemElectric)entityItem.item.getItem();
            electricItem.setJoules(tileEntity.electricityStored, entityItem.item);
            
            world.spawnEntityInWorld(entityItem);
        	
            if(Mekanism.hooks.IC2Loaded)
            {
            	EnergyNet.getForWorld(tileEntity.worldObj).removeTileEntity(tileEntity);
            }
            tileEntity.invalidate();
        }
	        
    	super.breakBlock(world, x, y, z, i1, i2);
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		for(EnumTier tier : EnumTier.values())
		{
			ItemStack discharged = new ItemStack(this);
			discharged.setItemDamage(100);
			((ItemBlockEnergyCube)discharged.getItem()).setTier(discharged, tier);
			list.add(discharged);
			ItemStack charged = new ItemStack(this);
			((ItemBlockEnergyCube)charged.getItem()).setTier(charged, tier);
			((ItemBlockEnergyCube)charged.getItem()).setJoules(tier.MAX_ELECTRICITY, charged);
			list.add(charged);
		};
	}
	
	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
        if(world.isRemote)
        {
            return true;
        }
        else {
        	TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
        	int metadata = world.getBlockMetadata(x, y, z);
        	
            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(Mekanism.instance, 8, world, x, y, z);
            		return true;
            	}
            }
        }
        return false;
    }
    
    @Override
    public String getTextureFile()
    {
    	return "/resources/mekanism/textures/terrain.png";
    }
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityEnergyCube();
	}
}
