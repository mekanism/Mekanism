package mekanism.common;

import java.util.List;
import java.util.Random;

import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.multiblock.IMultiBlock;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import mekanism.common.BlockMachine.MachineType;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

public class BlockAdvancedSolarGenerator extends BlockContainer
{
	public Random machineRand = new Random();
	
	public BlockAdvancedSolarGenerator(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
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
    	
        if(tileEntity instanceof IMultiBlock)
        {
        	((IMultiBlock)tileEntity).onCreate(new Vector3(x, y, z));
        }
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);

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
            
            if(tileEntity instanceof IMultiBlock)
            {
            	((IMultiBlock)tileEntity).onDestroy(tileEntity);
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
        
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	
        if (tileEntity != null)
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
    public String getTextureFile()
    {
    	return "/resources/mekanism/textures/terrain.png";
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
    	return new TileEntityAdvancedSolarGenerator();
    }
	
    /*This method is not used, metadata manipulation is required to create a Tile Entity.*/
    @Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        boolean canPlace = super.canPlaceBlockAt(world, x, y, z);
        
        int idSum = 0;
        idSum += world.getBlockId(x, y, z);
        World worldObj = world;
        
		for(int xPos=-1;xPos<=2;xPos++)
		{
			for(int zPos=-1;zPos<=2;zPos++)
			{
				idSum += worldObj.getBlockId(x+xPos, y+2, z+zPos);
			}
		}
		
		return (idSum == 0) && canPlace;
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
		return Mekanism.RENDER_ID;
	}
}
