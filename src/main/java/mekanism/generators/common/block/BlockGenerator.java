package mekanism.generators.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.MekanismConfig.client;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.render.ctm.CTMBlockRenderContext;
import mekanism.client.render.ctm.CTMData;
import mekanism.client.render.ctm.ICTMBlock;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.GeneratorsItems;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.states.BlockStateGenerator;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlock;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.tools.IToolWrench;

/**
 * Block class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Wind Generator
 * 7: Turbine Rotor
 * 8: Rotational Complex
 * 9: Electromagnetic Coil
 * 10: Turbine Casing
 * 11: Turbine Valve
 * 12: Turbine Vent
 * 13: Saturating Condenser
 * @author AidanBrady
 *
 */
public abstract class BlockGenerator extends BlockContainer implements ICTMBlock
{
	public CTMData[] ctmData = new CTMData[16];
	
	public Random machineRand = new Random();
	
	private static final AxisAlignedBB SOLAR_BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
	private static final AxisAlignedBB ROTOR_BOUNDS = new AxisAlignedBB(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);

	public BlockGenerator()
	{
		super(Material.IRON);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
		
		initCTMs();
	}
	
	public static BlockGenerator getGeneratorBlock(GeneratorBlock block)
	{
		return new BlockGenerator()
		{
			@Override
			public GeneratorBlock getGeneratorBlock()
			{
				return block;
			}
		};
	}

	public abstract GeneratorBlock getGeneratorBlock();
	
	public void initCTMs()
	{
		switch(getGeneratorBlock())
		{
			case GENERATOR_BLOCK_1:
				ctmData[9] = new CTMData(GeneratorType.ELECTROMAGNETIC_COIL);
				ctmData[10] = new CTMData(GeneratorType.TURBINE_CASING, GeneratorType.TURBINE_VALVE, GeneratorType.TURBINE_VENT);
				ctmData[11] = new CTMData(GeneratorType.TURBINE_VALVE, GeneratorType.TURBINE_CASING, GeneratorType.TURBINE_VENT);
				ctmData[12] = new CTMData(GeneratorType.TURBINE_VENT, GeneratorType.TURBINE_CASING, GeneratorType.TURBINE_VALVE);
				ctmData[13] = new CTMData(GeneratorType.SATURATING_CONDENSER);
				
				break;
		}
	}
	
	@SideOnly(Side.CLIENT)
    @Override
    public IBlockState getExtendedState(IBlockState stateIn, IBlockAccess w, BlockPos pos) 
	{
        if(stateIn.getBlock() == null || stateIn.getMaterial() == Material.AIR) 
        {
            return stateIn;
        }
        
        IExtendedBlockState state = (IExtendedBlockState)stateIn;
        CTMBlockRenderContext ctx = new CTMBlockRenderContext(w, pos);

        return state.withProperty(BlockStateBasic.ctmProperty, ctx);
    }
	
	@Override
	public BlockStateContainer createBlockState()
	{
		return new BlockStateGenerator(this, getTypeProperty());
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		GeneratorType type = GeneratorType.get(getGeneratorBlock(), meta & 0xF);

		return getDefaultState().withProperty(getTypeProperty(), type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		GeneratorType type = state.getValue(getTypeProperty());
		return type.meta;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		
		if(tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock)tile).facing != null)
		{
			state = state.withProperty(BlockStateFacing.facingProperty, ((TileEntityBasicBlock)tile).facing);
		}
		
		if(tile instanceof IActiveState)
		{
			state = state.withProperty(BlockStateGenerator.activeProperty, ((IActiveState)tile).getActive());
		}
		
		return state;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			
			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).doUpdate();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack itemstack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);

		int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(entityliving.rotationPitch);
		int change = 3;

		if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
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

		if(change != 0 && change != 1)
		{
			switch(side)
			{
				case 0: change = 2; break;
				case 1: change = 5; break;
				case 2: change = 3; break;
				case 3: change = 4; break;
			}
		}

		tileEntity.setFacing((short)change);
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(pos) > 0;

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
		
		if(!world.isRemote && tileEntity instanceof IMultiblock)
		{
			((IMultiblock)tileEntity).doUpdate();
		}
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		if(client.enableAmbientLighting)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof IActiveState && !(tileEntity instanceof TileEntitySolarGenerator))
			{
				if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
				{
					return client.ambientLightingLevel;
				}
			}
		}

		return 0;
	}


	@Override
	public int damageDropped(IBlockState state)
	{
		return state.getBlock().getMetaFromState(state);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item i, CreativeTabs creativetabs, List list)
	{
		for(GeneratorType type : GeneratorType.values())
		{
			list.add(new ItemStack(i, 1, type.meta));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random)
	{
		GeneratorType type = GeneratorType.get(state.getBlock(), state.getBlock().getMetaFromState(state));
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		
		if(MekanismUtils.isActive(world, pos))
		{
			float xRandom = (float)pos.getX() + 0.5F;
			float yRandom = (float)pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)pos.getZ() + 0.5F;
			float iRandom = 0.52F;
			float jRandom = random.nextFloat() * 0.6F - 0.3F;

			if(tileEntity.facing == EnumFacing.WEST)
			{
				switch(type)
				{
					case HEAT_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle(EnumParticleTypes.FLAME, (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.25, pos.getY()+.2, pos.getZ()+.5, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == EnumFacing.EAST)
			{
				switch(type)
				{
					case HEAT_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)(xRandom + iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle(EnumParticleTypes.FLAME, (double)(xRandom + iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.75, pos.getY()+.2, pos.getZ()+.5, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == EnumFacing.NORTH)
			{
				switch(type)
				{
					case HEAT_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle(EnumParticleTypes.FLAME, (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.5, pos.getY()+.2, pos.getZ()+.25, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == EnumFacing.SOUTH)
			{
				switch(type)
				{
					case HEAT_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle(EnumParticleTypes.FLAME, (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX()+.5, pos.getY()+.2, pos.getZ()+.75, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		
		if(!world.isRemote && tileEntity instanceof TileEntityTurbineRotor)
		{
			int amount = ((TileEntityTurbineRotor)tileEntity).getHousedBlades();
			
			if(amount > 0)
			{
				float motion = 0.7F;
				double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

				EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, new ItemStack(GeneratorsItems.TurbineBlade, amount));

				world.spawnEntityInWorld(entityItem);
			}
		}

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, ItemStack stack, EnumFacing side, float playerX, float playerY, float playerZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		int metadata = state.getBlock().getMetaFromState(state);

		if(stack != null)
		{
			Item tool = stack.getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, pos))
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					if(entityplayer.isSneaking())
					{
						dismantleBlock(state, world, pos, false);
						
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, pos);
					}
	
					int change = tileEntity.facing.rotateY().ordinal();
	
					tileEntity.setFacing((short)change);
					world.notifyNeighborsOfStateChange(pos, this);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
			}
		}
		
		if(metadata == GeneratorType.TURBINE_CASING.meta || metadata == GeneratorType.TURBINE_VALVE.meta || metadata == GeneratorType.TURBINE_VENT.meta)
		{
			return ((IMultiblock)tileEntity).onActivate(entityplayer, hand, stack);
		}
		
		if(metadata == GeneratorType.TURBINE_ROTOR.meta)
		{
			TileEntityTurbineRotor rod = (TileEntityTurbineRotor)tileEntity;
			
			if(!entityplayer.isSneaking())
			{
				if(stack != null && stack.getItem() == GeneratorsItems.TurbineBlade)
				{
					if(!world.isRemote && rod.editBlade(true))
					{
						if(!entityplayer.capabilities.isCreativeMode)
						{
							stack.stackSize--;
							
							if(stack.stackSize == 0)
							{
								entityplayer.setHeldItem(hand, null);
							}
						}
					}
					
					return true;
				}
			}
			else {
				if(!world.isRemote)
				{
					if(stack == null)
					{
						if(rod.editBlade(false))
						{
							if(!entityplayer.capabilities.isCreativeMode)
							{
								entityplayer.setHeldItem(hand, new ItemStack(GeneratorsItems.TurbineBlade));
								entityplayer.inventory.markDirty();
							}
						}
					}
					else if(stack.getItem() == GeneratorsItems.TurbineBlade)
					{
						if(stack.stackSize < stack.getMaxStackSize())
						{
							if(rod.editBlade(false))
							{
								if(!entityplayer.capabilities.isCreativeMode)
								{
									stack.stackSize++;
									entityplayer.inventory.markDirty();
								}
							}
						}
					}
				}
				
				return true;
			}
			
			return false;
		}
		
		int guiId = GeneratorType.get(getGeneratorBlock(), metadata).guiId;

		if(guiId != -1 && tileEntity != null)
		{
			if(!entityplayer.isSneaking())
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					entityplayer.openGui(MekanismGenerators.instance, guiId, world, pos.getX(), pos.getY(), pos.getZ());
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
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
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		int metadata = state.getBlock().getMetaFromState(state);
		
		if(GeneratorType.get(getGeneratorBlock(), metadata) == null)
		{
			return null;
		}

		return GeneratorType.get(getGeneratorBlock(), metadata).create();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return null;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
	
	/*This method is not used, metadata manipulation is required to create a Tile Entity.*/
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		GeneratorType type = GeneratorType.get(state);

		switch(type)
		{
			case SOLAR_GENERATOR:
				return SOLAR_BOUNDS;
			case TURBINE_ROTOR:
				return ROTOR_BOUNDS;
			default:
				return super.getBoundingBox(state, world, pos);
		}
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(state, null, world, pos, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(pos);
	}

	@Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		ItemStack itemStack = new ItemStack(GeneratorsBlocks.Generator, 1, state.getBlock().getMetaFromState(state));

		if(itemStack.getTagCompound() == null && !(tileEntity instanceof TileEntityMultiblock))
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		if(tileEntity == null)
		{
			return null;
		}
		
		if(tileEntity instanceof ISecurityTile)
		{
			ISecurityItem securityItem = (ISecurityItem)itemStack.getItem();
			
			if(securityItem.hasSecurity(itemStack))
			{
				securityItem.setOwner(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwner());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}

		if(tileEntity instanceof TileEntityElectricBlock)
		{
			IEnergizedItem electricItem = (IEnergizedItem)itemStack.getItem();
			electricItem.setEnergy(itemStack, ((TileEntityElectricBlock)tileEntity).electricityStored);
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).handleInventory())
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((TileEntityContainerBlock)tileEntity).getInventory(), itemStack);
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
		{
			if(tileEntity instanceof ISustainedTank)
			{
				if(((ISustainedTank)tileEntity).getFluidStack() != null)
				{
					((ISustainedTank)itemStack.getItem()).setFluidStack(((ISustainedTank)tileEntity).getFluidStack(), itemStack);
				}
			}
		}

		return itemStack;
	}

	public ItemStack dismantleBlock(IBlockState state, World world, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(state, null, world, pos, null);

		world.setBlockToAir(pos);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		GeneratorType type = GeneratorType.get(getGeneratorBlock(), state.getBlock().getMetaFromState(state));

		if(type != GeneratorType.SOLAR_GENERATOR && 
				type != GeneratorType.ADVANCED_SOLAR_GENERATOR && 
				type != GeneratorType.WIND_GENERATOR &&
				type != GeneratorType.TURBINE_ROTOR)
		{
			return true;
		}

		return false;
	}

	@Override
	public EnumFacing[] getValidRotations(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		EnumFacing[] valid = new EnumFacing[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(EnumFacing dir : EnumFacing.VALUES)
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public CTMData getCTMData(IBlockState state)
	{
		return ctmData[state.getBlock().getMetaFromState(state)];
	}
	
	@Override
	public String getOverrideTexture(IBlockState state, EnumFacing side)
	{
		return null;
	}
	
	@Override
	public PropertyEnum<GeneratorType> getTypeProperty()
	{
		return getGeneratorBlock().getProperty();
	}
}
