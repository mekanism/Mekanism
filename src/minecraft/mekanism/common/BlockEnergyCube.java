package mekanism.common;

import java.util.List;
import java.util.Random;

import buildcraft.api.tools.IToolWrench;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import universalelectricity.core.item.IItemElectric;
import universalelectricity.prefab.implement.IToolConfigurator;

import mekanism.common.Tier.EnergyCubeTier;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Block class for handling multiple energy cube block IDs. 
 * 0: Basic Energy Cube
 * 1: Advanced Energy Cube
 * 2: Elite Energy Cube
 * @author AidanBrady
 *
 */
public class BlockEnergyCube extends BlockContainer
{
	public Icon[][] icons = new Icon[256][256];
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
	public void registerIcons(IconRegister register)
	{
		icons[0][0] = register.registerIcon("mekanism:BasicEnergyCubeFront");
		icons[0][1] = register.registerIcon("mekanism:BasicEnergyCubeSide");
		icons[1][0] = register.registerIcon("mekanism:AdvancedEnergyCubeFront");
		icons[1][1] = register.registerIcon("mekanism:AdvancedEnergyCubeSide");
		icons[2][0] = register.registerIcon("mekanism:EliteEnergyCubeFront");
		icons[2][1] = register.registerIcon("mekanism:EliteEnergyCubeSide");
		icons[3][0] = register.registerIcon("mekanism:UltimateEnergyCubeFront");
		icons[3][1] = register.registerIcon("mekanism:UltimateEnergyCubeSide");
	}
	
	@Override
	public void setBlockBoundsForItemRender()
	{
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	public Icon getIcon(int side, int meta)
	{
		if(side == 3)
		{
			return icons[meta][0];
		}
		else {
			return icons[meta][1];
		}
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
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
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
    	
    	if(side == tileEntity.facing)
    	{
    		return icons[tileEntity.tier.ordinal()][0];
    	}
    	else {
    		return icons[tileEntity.tier.ordinal()][1];
    	}
    }
    
    @Override
    public int quantityDropped(Random random)
    {
    	return 0;
    }
    
    @Override
    public int idDropped(int i, Random random, int j)
    {
    	return 0;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			ItemStack discharged = new ItemStack(this);
			discharged.setItemDamage(100);
			((ItemBlockEnergyCube)discharged.getItem()).setEnergyCubeTier(discharged, tier);
			list.add(discharged);
			ItemStack charged = new ItemStack(this);
			((ItemBlockEnergyCube)charged.getItem()).setEnergyCubeTier(charged, tier);
			((ItemBlockEnergyCube)charged.getItem()).setJoules(tier.MAX_ELECTRICITY, charged);
			list.add(charged);
		};
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
        	
        	if(entityplayer.getCurrentEquippedItem() != null)
        	{
    	    	if(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolConfigurator)
    	    	{
    	    		((IToolConfigurator)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
    	    		
    	    		int change = 0;
    	    		
    	    		switch(tileEntity.facing)
    	    		{
    	    			case 3:
    	    				change = 5;
    	    				break;
    	    			case 5:
    	    				change = 2;
    	    				break;
    	    			case 2:
    	    				change = 4;
    	    				break;
    	    			case 4:
    	    				change = 1;
    	    				break;
    	    			case 1:
    	    				change = 0;
    	    				break;
    	    			case 0:
    	    				change = 3;
    	    				break;
    	    		}
    	    		
    	    		tileEntity.setFacing((short)change);
    	    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
    	    		return true;
    	    	}
    	    	else if(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench && !entityplayer.getCurrentEquippedItem().getItemName().contains("omniwrench"))
    	    	{
    	    		if(entityplayer.isSneaking())
    	    		{
    	    			dismantleBlock(world, x, y, z, false);
    	    			return true;
    	    		}
    	    		
    	    		((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
    	    		
    	    		int change = 0;
    	    		
    	    		switch(tileEntity.facing)
    	    		{
    	    			case 3:
    	    				change = 5;
    	    				break;
    	    			case 5:
    	    				change = 2;
    	    				break;
    	    			case 2:
    	    				change = 4;
    	    				break;
    	    			case 4:
    	    				change = 1;
    	    				break;
    	    			case 1:
    	    				change = 0;
    	    				break;
    	    			case 0:
    	    				change = 3;
    	    				break;
    	    		}
    	    		
    	    		tileEntity.setFacing((short)change);
    	    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
    	    		return true;
    	    	}
        	}
        	
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
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
	    	TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
	    	
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));
	        
	        world.spawnEntityInWorld(entityItem);
    	}
    	
        return world.setBlockToAir(x, y, z);
    }
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityEnergyCube();
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
    	TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.EnergyCube);
        
        IEnergyCube energyCube = (IEnergyCube)itemStack.getItem();
        energyCube.setEnergyCubeTier(itemStack, tileEntity.tier);
        
        IItemElectric electricItem = (IItemElectric)itemStack.getItem();
        electricItem.setJoules(tileEntity.electricityStored, itemStack);
        
        ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
        inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
        
        return itemStack;
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z);
        
        world.setBlockToAir(x, y, z);
        
        if(!returnBlock)
        {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);
	        
            world.spawnEntityInWorld(entityItem);
        }
        
        return itemStack;
	}
}
