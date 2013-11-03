package mekanism.common.block;

import java.util.Random;

import mekanism.api.IStorageTank;
import mekanism.common.ISustainedInventory;
import mekanism.common.Mekanism;
import mekanism.common.tileentity.TileEntityBasicBlock;
import mekanism.common.tileentity.TileEntityElectricBlock;
import mekanism.common.tileentity.TileEntityGasTank;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGasTank extends BlockContainer
{
	public Random machineRand = new Random();
	
	public BlockGasTank(int id)
	{
		super(id, Material.iron);
		setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 1.0F, 0.8F);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
    {
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    	
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
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
    {
    	if(world.isRemote)
    	{
    		return true;
    	}
    	
    	TileEntityGasTank tileEntity = (TileEntityGasTank)world.getBlockTileEntity(x, y, z);
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(entityplayer.getCurrentEquippedItem() != null)
    	{
    		Item tool = entityplayer.getCurrentEquippedItem().getItem();
    		
	    	if(tool instanceof IToolWrench && !tool.getUnlocalizedName().contains("omniwrench"))
	    	{
	    		if(((IToolWrench)tool).canWrench(entityplayer, x, y, z))
	    		{
		    		if(entityplayer.isSneaking())
		    		{
		    			dismantleBlock(world, x, y, z, false);
		    			return true;
		    		}
		    		
		    		((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);
		    		
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
		    				change = 3;
		    				break;
		    		}
		    		
		    		tileEntity.setFacing((short)change);
		    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
		    		return true;
	    		}
	    	}
    	}
    	
        if(tileEntity != null)
        {
        	if(!entityplayer.isSneaking())
        	{
        		entityplayer.openGui(Mekanism.instance, 10, world, x, y, z);
        		return true;
        	}
        }
    	return false;
    }
    
    @Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));
	        
	        world.spawnEntityInWorld(entityItem);
    	}
    	
        return world.setBlockToAir(x, y, z);
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
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public int getRenderType()
	{
		return -1;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityGasTank();
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		TileEntityGasTank tileEntity = (TileEntityGasTank)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.GasTank);
        
        IStorageTank storageTank = (IStorageTank)itemStack.getItem();
        storageTank.setGasType(itemStack, tileEntity.gasType);
        storageTank.setGas(tileEntity.gasType, tileEntity.gasStored, itemStack);
        
        ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
        inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
        
		return itemStack;
	}
}
