package mekanism.common;

import java.util.List;
import java.util.Random;

import universalelectricity.prefab.implement.IToolConfigurator;

import mekanism.api.IActiveState;
import mekanism.client.ClientProxy;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Platinum Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * 5: Basic Smelting Factory
 * 6: Advanced Smelting Factory
 * 7: Elite Smelting Factory
 * 8: Metallurgic Infuser
 * @author AidanBrady
 *
 */
public class BlockMachine extends BlockContainer
{
	public Random machineRand = new Random();
	
	public BlockMachine(int id)
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
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
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
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        if (MekanismUtils.isActive(world, x, y, z))
        {
            float xRandom = (float)x + 0.5F;
            float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float)z + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;

            if (tileEntity.facing == 4)
            {
                world.spawnParticle("smoke", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 5)
            {
                world.spawnParticle("smoke", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 2)
            {
                world.spawnParticle("smoke", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 3)
            {
                world.spawnParticle("smoke", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
            }
        }
    }
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive())
			{
				return 15;
			}
		}
		
		return 0;
	}
    
	@Override
    public int getBlockTextureFromSideAndMetadata(int side, int meta)
    {
    	if(meta == 0)
    	{
        	if(side == 3)
        	{
        		return 9;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 1)
    	{
        	if(side == 3)
        	{
        		return 14;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 2)
    	{
        	if(side == 3)
        	{
        		return 15;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 3)
    	{
        	if(side == 3)
        	{
        		return 13;
        	}
        	else {
        		return 2;
        	}
    	}
    	else if(meta == 4)
    	{
        	if(side == 0 || side == 1)
        	{
        		return 18;
        	}
        	else if(side == 3)
        	{
        		return 16;
        	}
        	else {
        		return 19;
        	}
    	}
    	else if(meta == 5)
    	{
    		if(side == 3)
    		{
    			return 41;
    		}
    		else {
    			return 44;
    		}
    	}
    	else if(meta == 6)
    	{
    		if(side == 3)
    		{
    			return 42;
    		}
    		else {
    			return 45;
    		}
    	}
    	else if(meta == 7)
    	{
    		if(side == 3)
    		{
    			return 43;
    		}
    		else {
    			return 46;
    		}
    	}
    	else if(meta == 8)
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
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        
    	if(metadata == 0)
    	{
	        if(side == tileEntity.facing)
	        {
	        	return MekanismUtils.isActive(world, x, y, z) ? 8 : 9;
	        }
	        else {
	        	return 2;
	        }
    	}
    	else if(metadata == 1)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+2 : 14;
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 2)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+3 : 15;
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 3)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+1 : 13;
            }
            else {
            	return 2;
            }
    	}
    	else if(metadata == 4)
    	{
            if(side == 0 || side == 1)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? 20 : 18;
            }
            else {
            	if(side == tileEntity.facing)
            	{
            		return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+4 : 16;
            	}
            	else if(side == ForgeDirection.getOrientation(tileEntity.facing).getOpposite().ordinal())
            	{
            		return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+5 : 17;
            	}
            	else {
            		return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+6 : 19;
            	}
            }
    	}
    	else if(metadata == 5)
    	{
    		if(side == tileEntity.facing)
    		{
    			return 41;
    		}
    		else {
    			return 44;
    		}
    	}
    	else if(metadata == 6)
    	{
    		if(side == tileEntity.facing)
    		{
    			return 42;
    		}
    		else {
    			return 45;
    		}
    	}
    	else if(metadata == 7)
    	{
    		if(side == tileEntity.facing)
    		{
    			return 43;
    		}
    		else {
    			return 46;
    		}
    	}
    	else if(metadata == 8)
    	{
    		if(side == tileEntity.facing)
    		{
    			return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+7 : 33;
    		}
    		else {
    			return MekanismUtils.isActive(world, x, y, z) ? Mekanism.ANIMATED_TEXTURE_INDEX+8 : 32;
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
		
		if(Mekanism.extrasEnabled)
		{
			list.add(new ItemStack(i, 1, 4));
		}
		
		list.add(new ItemStack(i, 1, 5));
		list.add(new ItemStack(i, 1, 6));
		list.add(new ItemStack(i, 1, 7));
		list.add(new ItemStack(i, 1, 8));
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
    	else {
        	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        	int metadata = world.getBlockMetadata(x, y, z);
        	
        	if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() instanceof IToolConfigurator)
        	{
        		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
        		((IToolConfigurator)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
        		
        		int change = 0;
        		
        		switch(tileEntity.facing)
        		{
        			case 3:
        				change = 4;
        				break;
        			case 4:
        				change = 5;
        				break;
        			case 5:
        				change = 2;
        				break;
        			case 2:
        				change = 3;
        				break;
        		}
        		
        		tileEntity.setFacing((short)change);
        		return true;
        	}
        	
            if (tileEntity != null)
            {
            	if(!entityplayer.isSneaking())
            	{
            		entityplayer.openGui(Mekanism.instance, MachineType.getFromMetadata(metadata).guiId, world, x, y, z);
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
    public TileEntity createNewTileEntity(World world, int metadata)
    {
    	return MachineType.getFromMetadata(metadata).create();
    }
	
    @Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
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
		return ClientProxy.RENDER_ID;
	}
	
	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(0, 3, TileEntityEnrichmentChamber.class, false),
		PLATINUM_COMPRESSOR(1, 4, TileEntityPlatinumCompressor.class, false),
		COMBINER(2, 5, TileEntityCombiner.class, false),
		CRUSHER(3, 6, TileEntityCrusher.class, false),
		THEORETICAL_ELEMENTIZER(4, 7, TileEntityTheoreticalElementizer.class, true),
		BASIC_SMELTING_FACTORY(5, 11, TileEntitySmeltingFactory.class, false),
		ADVANCED_SMELTING_FACTORY(6, 11, TileEntityAdvancedSmeltingFactory.class, false),
		ELITE_SMELTING_FACTORY(7, 11, TileEntityEliteSmeltingFactory.class, false),
		METALLURGIC_INFUSER(8, 12, TileEntityMetallurgicInfuser.class, false);
		
		public int meta;
		public int guiId;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;
		
		private MachineType(int i, int j, Class<? extends TileEntity> tileClass, boolean model)
		{
			meta = i;
			guiId = j;
			tileEntityClass = tileClass;
			hasModel = model;
		}
		
		public static MachineType getFromMetadata(int meta)
		{
			return values()[meta];
		}
		
		public TileEntity create()
		{
			TileEntity tileEntity;
			
			try {
				tileEntity = tileEntityClass.newInstance();
				return tileEntity;
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
