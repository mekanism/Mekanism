package net.uberkat.obsidian.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.uberkat.obsidian.common.BlockMachine.MachineType;

public class BlockGenerator extends BlockContainer
{
	public Random machineRand = new Random();
	
	public BlockGenerator(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(ObsidianIngots.tabOBSIDIAN);
		setRequiresSelfNotify();
	}
	
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
    	TileEntityGenerator tileEntity = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
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
    
    public int getBlockTextureFromSideAndMetadata(int side, int meta)
    {
    	if(meta == 0)
    	{
    		if(side == 3)
    		{
    			return 27;
    		}
    		else if(side != 0 && side != 1)
        	{
        		return 25;
        	}
        	else {
        		return 26;
        	}
    	}
    	else {
    		return 0;
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityGenerator tileEntity = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
        
    	if(metadata == 0)
    	{
	        if(side == tileEntity.facing)
	        {
	        	return 27;
	        }
	        else if(side != 0 && side != 1)
	        {
	        	return 25;
	        }
	        else {
	        	return 26;
	        }
    	}
    	else {
    		return 0;
    	}
    }
    
    public int damageDropped(int i)
    {
    	return i;
    }
    
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
	}
    
	/**
	 * Checks if a generator is operating.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return if generator is operating
	 */
    public boolean isActive(IBlockAccess world, int x, int y, int z)
    {
    	TileEntityGenerator tileEntity = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
    	if(tileEntity != null)
    	{
    		return tileEntity.canPower();
    	}
    	return false;
    }
    
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
        TileEntityGenerator tileEntity = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);

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
                            item.item.setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
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
    
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
        	TileEntityGenerator tileEntity = (TileEntityGenerator)world.getBlockTileEntity(x, y, z);
        	int metadata = world.getBlockMetadata(x, y, z);
        	
            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		int id = 0;
            		
            		if(metadata == 0) id = 9;
            		
            		entityplayer.openGui(ObsidianIngots.instance, id, world, x, y, z);
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
    	if(metadata == MachineType.HEAT_GENERATOR.index)
    	{
    		return new TileEntityHeatGenerator();
    	}
    	else {
    		return null;
    	}
    }
	
    /*This method is not used, metadata manipulation is required to create a Tile Entity.*/
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	public static enum MachineType
	{
		HEAT_GENERATOR(0);
		
		private int index;
		
		private MachineType(int i)
		{
			index = i;
		}
	}
}
