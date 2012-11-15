package mekanism.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import mekanism.common.BlockMachine.MachineType;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;

/**
 * Block class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 4: Hydrogen Generator
 * @author AidanBrady
 *
 */
public class BlockGenerator extends BlockContainer
{
	public Random machineRand = new Random();
	
	public BlockGenerator(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
		setRequiresSelfNotify();
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving)
    {
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    	
    	//If the block is a electrolytic separator.
    	if(world.getBlockMetadata(x, y, z) == 2)
    	{
    		boolean hasReactor = false;
    		//Loop through all possible orientations.
    		for(ForgeDirection direction : ForgeDirection.values())
    		{
    			int xPos = x + direction.offsetX;
    			int yPos = y + direction.offsetY;
    			int zPos = z + direction.offsetZ;
    			
    			//If this orientation faces a hydrogen reactor.
    			if(world.getBlockId(xPos, yPos, zPos) == Mekanism.generatorID && world.getBlockMetadata(xPos, yPos, zPos) == 3)
    			{
    				hasReactor = true;
    				//Set the separator's facing towards the reactor.
    				tileEntity.setFacing((short)direction.ordinal());
    				break;
    			}
    		}
    		
    		//If there was a reactor next to this machine, no further calculations are needed.
    		if(hasReactor)
    		{
    			return;
    		}
    	}
    	
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
    	else if(meta == 1)
    	{
    		if(side == 3)
    		{
    			return 30;
    		}
    		else if(side == 1)
    		{
    			return 28;
    		}
    		else {
    			return 29;
    		}
    	}
    	else if(meta == 2)
    	{
    		if(side == 3)
    		{
    			return 34;
    		}
    		else {
    			return 35;
    		}
    	}
    	else if(meta == 3)
    	{
    		if(side == 3)
    		{
    			return 33;
    		}
    		else {
    			return 32;
    		}
    	}
    	else {
    		return 0;
    	}
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public int getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
        
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
    	else if(metadata == 1)
    	{
    		if(side == tileEntity.facing)
    		{
    			return 30;
    		}
    		else if(side == 1)
    		{
    			return 28;
    		}
    		else {
    			return 29;
    		}
    	}
    	else if(metadata == 2)
    	{
    		if(side == tileEntity.facing)
    		{
    			return 34;
    		}
    		else {
    			return 35;
    		}
    	}
    	else if(metadata == 3)
    	{
    		TileEntityHydrogenGenerator generator = (TileEntityHydrogenGenerator)world.getBlockTileEntity(x, y, z);
    		if(side == tileEntity.facing)
    		{
    			return generator.isActive ? Mekanism.ANIMATED_TEXTURE_INDEX+5 : 33;
    		}
    		else {
    			return generator.isActive ? Mekanism.ANIMATED_TEXTURE_INDEX+6 : 32;
    		}
    	}
    	else {
    		return 0;
    	}
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
            tileEntity.invalidate();
        }
	        
    	super.breakBlock(world, x, y, z, i1, i2);
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
        if(world.isRemote)
        {
            return true;
        }
        
        if(metadata == 3 && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().isItemEqual(new ItemStack(Mekanism.Generator, 1, 2)))
        {
        	if(((TileEntityBasicBlock)world.getBlockTileEntity(x, y, z)).facing != facing)
        	{
        		return false;
        	}
        }
        
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	
        if (tileEntity != null)
        {
        	if(!entityplayer.isSneaking())
        	{
        		entityplayer.openGui(Mekanism.instance, GeneratorType.getGuiID(metadata), world, x, y, z);
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
    	if(metadata == GeneratorType.HEAT_GENERATOR.meta)
    	{
    		return new TileEntityHeatGenerator();
    	}
    	else if(metadata == GeneratorType.SOLAR_GENERATOR.meta)
    	{
    		return new TileEntitySolarGenerator();
    	}
    	else if(metadata == GeneratorType.ELECTROLYTIC_SEPARATOR.meta)
    	{
    		return new TileEntityElectrolyticSeparator();
    	}
    	else if(metadata == GeneratorType.HYDROGEN_GENERATOR.meta)
    	{
    		return new TileEntityHydrogenGenerator();
    	}
    	else {
    		return null;
    	}
    }
	
    /*This method is not used, metadata manipulation is required to create a Tile Entity.*/
    @Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
	
	public static enum GeneratorType
	{
		HEAT_GENERATOR(0, 9),
		SOLAR_GENERATOR(1, 10),
		ELECTROLYTIC_SEPARATOR(2, 11),
		HYDROGEN_GENERATOR(3, 12);
		
		private int meta;
		private int guiId;
		
		private GeneratorType(int i, int j)
		{
			meta = i;
			guiId = j;
		}
		
		public static int getGuiID(int meta)
		{
			return values()[meta].guiId;
		}
		
		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}
}
