package mekanism.common;

import java.util.Random;

import universalelectricity.prefab.implement.IToolConfigurator;
import buildcraft.api.tools.IToolWrench;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGasTank extends BlockContainer
{
	public Icon[] icons = new Icon[256];
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
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:GasTankTop");
		icons[1] = register.registerIcon("mekanism:GasTankSide");
		icons[2] = register.registerIcon("mekanism:GasTankFront");
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
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
	@SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
	{
		if(side == 3)
		{
			return icons[2];
		}
		else if(side == 0 || side == 1)
		{
			return icons[0];
		}
		else {
			return icons[1];
		}
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
		
		if(side == tileEntity.facing)
		{
			return icons[2];
		}
		else if(side == 0 || side == 1)
		{
			return icons[0];
		}
		else {
			return icons[1];
		}
	}
    
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
    	TileEntityContainerBlock tileEntity = (TileEntityContainerBlock)world.getBlockTileEntity(x, y, z);

        if (tileEntity != null)
        {
            for (int i = 0; i < tileEntity.getSizeInventory(); ++i)
            {
                ItemStack slotStack = tileEntity.getStackInSlot(i);

                if (slotStack != null)
                {
                    float xRandom = machineRand.nextFloat() * 0.8F + 0.1F;
                    float yRandom = machineRand.nextFloat() * 0.8F + 0.1F;
                    float zRandom = machineRand.nextFloat() * 0.8F + 0.1F;

                    while (slotStack.stackSize > 0)
                    {
                        int j = machineRand.nextInt(21) + 10;

                        if (j > slotStack.stackSize)
                        {
                            j = slotStack.stackSize;
                        }

                        slotStack.stackSize -= j;
                        EntityItem item = new EntityItem(world, (double)((float)x + xRandom), (double)((float)y + yRandom), (double)((float)z + zRandom), new ItemStack(slotStack.itemID, j, slotStack.getItemDamage()));

                        if (slotStack.hasTagCompound())
                        {
                            item.getEntityItem().setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
                        }

                        float k = 0.05F;
                        item.motionX = (double)((float)machineRand.nextGaussian() * k);
                        item.motionY = (double)((float)machineRand.nextGaussian() * k + 0.2F);
                        item.motionZ = (double)((float)machineRand.nextGaussian() * k);
                        world.spawnEntityInWorld(item);
                    }
                }
        	}
            
            tileEntity.invalidate();
        }
	        
    	super.breakBlock(world, x, y, z, i1, i2);
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
	    				change = 3;
	    				break;
	    		}
	    		
	    		tileEntity.setFacing((short)change);
	    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
	    		return true;
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
    
	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.GasTank);
        
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
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityGasTank();
	}
}
