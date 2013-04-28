package mekanism.generators.common;

import java.util.List;
import java.util.Random;

import mekanism.api.IEnergizedItem;
import mekanism.common.IActiveState;
import mekanism.common.IBoundingBlock;
import mekanism.common.ISustainedInventory;
import mekanism.common.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.TileEntityBasicBlock;
import mekanism.common.TileEntityElectricBlock;
import mekanism.generators.client.GeneratorsClientProxy;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.prefab.implement.IToolConfigurator;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 2: Electrolytic Separator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Wind Turbine
 * @author AidanBrady
 *
 */
public class BlockGenerator extends BlockContainer
{
	public Icon[] solarSprites = new Icon[256];
	public Random machineRand = new Random();
	
	public BlockGenerator(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void registerIcons(IconRegister register)
	{
		solarSprites[0] = register.registerIcon("mekanism:SteelBlock");
		solarSprites[1] = register.registerIcon("mekanism:SolarGeneratorTop");
		solarSprites[2] = register.registerIcon("mekanism:SolarGeneratorSide");
		solarSprites[3] = register.registerIcon("mekanism:SolarGeneratorFront");
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
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
    			if(world.getBlockId(xPos, yPos, zPos) == MekanismGenerators.generatorID && world.getBlockMetadata(xPos, yPos, zPos) == 3)
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
        
        if(!GeneratorType.getFromMetadata(world.getBlockMetadata(x, y, z)).hasModel && tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
        {
	        if(height >= 65)
	        {
	        	change = 1;
	        }
	        else if(height <= -65)
	        {
	        	change = 0;
	        }
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
        
        if(tileEntity instanceof IBoundingBlock)
        {
        	((IBoundingBlock)tileEntity).onPlace();
        }
    }
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity instanceof IActiveState && !(tileEntity instanceof TileEntitySolarGenerator))
		{
			if(((IActiveState)tileEntity).getActive())
			{
				return 15;
			}
		}
		
		return 0;
	}
    
	@Override
    public Icon getIcon(int side, int meta)
    {
    	if(meta == 1)
    	{
    		if(side == 3)
    		{
    			return solarSprites[3];
    		}
    		else if(side == 1)
    		{
    			return solarSprites[1];
    		}
    		else if(side == 0)
    		{
    			return solarSprites[0];
    		}
    		else {
    			return solarSprites[2];
    		}
    	}
    	
    	return null;
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
        
    	if(metadata == 1)
    	{
    		if(side == tileEntity.facing)
    		{
    			return solarSprites[3];
    		}
    		else if(side == 1)
    		{
    			return solarSprites[1];
    		}
    		else if(side == 0)
    		{
    			return solarSprites[0];
    		}
    		else {
    			return solarSprites[2];
    		}
    	}
    	
    	return null;
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
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
		int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        if(MekanismUtils.isActive(world, x, y, z))
        {
            float xRandom = (float)x + 0.5F;
            float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float)z + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;

            if(tileEntity.facing == 4)
            {
            	switch(GeneratorType.values()[metadata])
            	{
            		case HEAT_GENERATOR:
            			world.spawnParticle("smoke", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
            			world.spawnParticle("flame", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
            			break;
            		case BIO_GENERATOR:
            			world.spawnParticle("smoke", x+.8, y+1, z+.8, 0.0D, 0.0D, 0.0D);
            			break;
            		default:
            			break;
            	}
            }
            else if(tileEntity.facing == 5)
            {
            	switch(GeneratorType.values()[metadata])
            	{
	            	case HEAT_GENERATOR:
	            		world.spawnParticle("smoke", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
	            		world.spawnParticle("flame", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
	        			break;
            		case BIO_GENERATOR:
            			world.spawnParticle("smoke", x+.2, y+1, z+.2, 0.0D, 0.0D, 0.0D);
            			break;
            		default:
            			break;
            	}
            }
            else if(tileEntity.facing == 2)
            {
            	switch(GeneratorType.values()[metadata])
            	{
	            	case HEAT_GENERATOR:
	            		world.spawnParticle("smoke", (double)(xRandom - jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
	            		world.spawnParticle("flame", (double)(xRandom - jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
	        			break;
            		case BIO_GENERATOR:
            			world.spawnParticle("smoke", x+.2, y+1, z+.8, 0.0D, 0.0D, 0.0D);
            			break;
            		default:
            			break;
            	}
            }
            else if(tileEntity.facing == 3)
            {
            	switch(GeneratorType.values()[metadata])
            	{
	            	case HEAT_GENERATOR:
	            		world.spawnParticle("smoke", (double)(xRandom - jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
	            		world.spawnParticle("flame", (double)(xRandom - jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
	        			break;
            		case BIO_GENERATOR:
            			world.spawnParticle("smoke", x+.8, y+1, z+.2, 0.0D, 0.0D, 0.0D);
            			break;
            		default:
            			break;
            	}
            }
        }
    }
	
	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
		if(world.getBlockMetadata(x, y, z) == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
	        boolean canPlace = super.canPlaceBlockAt(world, x, y, z);
	        
	        int idSum = 0;
	        idSum += world.getBlockId(x, y, z);
	        
			for(int xPos=-1;xPos<=2;xPos++)
			{
				for(int zPos=-1;zPos<=2;zPos++)
				{
					idSum += world.getBlockId(x+xPos, y+2, z+zPos);
				}
			}
			
			return (idSum == 0) && canPlace;
		}
		else if(world.getBlockMetadata(x, y, z) == GeneratorType.WIND_TURBINE.meta)
		{
	        boolean canPlace = super.canPlaceBlockAt(world, x, y, z);
	        
	        int idSum = 0;
	        
			for(int yPos = y+1; yPos <= y+4; yPos++)
			{
				idSum += world.getBlockId(x, yPos, z);
			}
			
			return (idSum == 0) && canPlace;
		}
		
		return super.canPlaceBlockAt(world, x, y, z);
    }
    
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);

        if(tileEntity instanceof IBoundingBlock)
        {
        	((IBoundingBlock)tileEntity).onBreak();
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
        
        if(metadata == 3 && entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().isItemEqual(new ItemStack(MekanismGenerators.Generator, 1, 2)))
        {
        	if(((TileEntityBasicBlock)world.getBlockTileEntity(x, y, z)).facing != facing)
        	{
        		return false;
        	}
        }
    	
        if (tileEntity != null)
        {
        	if(!entityplayer.isSneaking())
        	{
        		entityplayer.openGui(MekanismGenerators.instance, GeneratorType.getFromMetadata(metadata).guiId, world, x, y, z);
        		return true;
        	}
        }
        return false;
    }
    
    @Override
    public int quantityDropped(Random random)
    {
    	return 0;
    }
    
    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
    	return GeneratorType.getFromMetadata(metadata).create();
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
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return GeneratorsClientProxy.RENDER_ID;
	}
	
    /*This method is not used, metadata manipulation is required to create a Tile Entity.*/
    @Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
    
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) 
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == GeneratorType.SOLAR_GENERATOR.meta)
    	{
    		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.4F, 1.0F);
    	}
    	else {
    		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    	}
    }
    
    @Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
	    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
	    	
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
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(MekanismGenerators.Generator, 1, world.getBlockMetadata(x, y, z));
        
        IEnergizedItem electricItem = (IEnergizedItem)itemStack.getItem();
        electricItem.setEnergy(itemStack, tileEntity.electricityStored);
        
        ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
        inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
        
        if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
        {
        	if(tileEntity instanceof ISustainedTank)
        	{
        		if(((ISustainedTank)tileEntity).getLiquidStack() != null)
        		{
        			((ISustainedTank)itemStack.getItem()).setLiquidStack(((ISustainedTank)tileEntity).getLiquidStack(), itemStack);
        		}
        	}
        }
        
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
	
	public static enum GeneratorType
	{
		HEAT_GENERATOR(0, 0, 160000, TileEntityHeatGenerator.class, true),
		SOLAR_GENERATOR(1, 1, 96000, TileEntitySolarGenerator.class, false),
		ELECTROLYTIC_SEPARATOR(2, 2, 20000, TileEntityElectrolyticSeparator.class, true),
		HYDROGEN_GENERATOR(3, 3, 40000, TileEntityHydrogenGenerator.class, true),
		BIO_GENERATOR(4, 4, 160000, TileEntityBioGenerator.class, true),
		ADVANCED_SOLAR_GENERATOR(5, 1, 200000, TileEntityAdvancedSolarGenerator.class, true),
		WIND_TURBINE(6, 5, 200000, TileEntityWindTurbine.class, true);
		
		public int meta;
		public int guiId;
		public double maxEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;
		
		private GeneratorType(int i, int j, double k, Class<? extends TileEntity> tileClass, boolean model)
		{
			meta = i;
			guiId = j;
			maxEnergy = k;
			tileEntityClass = tileClass;
			hasModel = model;
		}
		
		public static GeneratorType getFromMetadata(int meta)
		{
			return values()[meta];
		}
		
		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				System.err.println("[Mekanism] Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}
}
