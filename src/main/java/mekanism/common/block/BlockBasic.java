package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.render.ctm.CTMBlockRenderContext;
import mekanism.client.render.ctm.CTMData;
import mekanism.client.render.ctm.ICTMBlock;
import mekanism.client.render.ctm.PropertyCTMRenderContext;
import mekanism.common.Mekanism;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.api.tools.IToolWrench;

/**
 * Block class for handling multiple metal block IDs.
 * 0:0: Osmium Block
 * 0:1: Bronze Block
 * 0:2: Refined Obsidian
 * 0:3: Charcoal Block
 * 0:4: Refined Glowstone
 * 0:5: Steel Block
 * 0:6: Bin
 * 0:7: Teleporter Frame
 * 0:8: Steel Casing
 * 0:9: Dynamic Tank
 * 0:10: Structural Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Thermal Evaporation Controller
 * 0:15: Thermal Evaporation Valve
 * 1:0: Thermal Evaporation Block
 * 1:1: Induction Casing
 * 1:2: Induction Port
 * 1:3: Induction Cell
 * 1:4: Induction Provider
 * 1:5: Superheating Element
 * 1:6: Pressure Disperser
 * 1:7: Boiler Casing
 * 1:8: Boiler Valve
 * 1:9: Security Desk
 * @author AidanBrady
 *
 */
public abstract class BlockBasic extends Block implements ICTMBlock//TODO? implements ICustomBlockIcon
{
	public static final PropertyCTMRenderContext ctmProperty = new PropertyCTMRenderContext();
	
	public CTMData[][] ctmData = new CTMData[16][4];
	
	public BlockBasic()
	{
		super(Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
		
		initCTMs();
	}

	public static BlockBasic getBlockBasic(BasicBlock block)
	{
		return new BlockBasic() {
			@Override
			public BasicBlock getBasicBlock()
			{
				return block;
			}
		};
	}

	public abstract BasicBlock getBasicBlock();

	@Override
	public BlockState createBlockState()
	{
		return new BlockStateBasic(this, getProperty());
	}

	public PropertyEnum<BlockStateBasic.BasicBlockType> getProperty()
	{
		return getBasicBlock().getProperty();
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		BlockStateBasic.BasicBlockType type = BlockStateBasic.BasicBlockType.get(getBasicBlock(), meta&0xF);

		return getDefaultState().withProperty(getProperty(), type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		BlockStateBasic.BasicBlockType type = state.getValue(getProperty());
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
			state = state.withProperty(BlockStateBasic.activeProperty, ((IActiveState)tile).getActive());
		}
		
		if(tile instanceof TileEntityInductionCell)
		{
			state = state.withProperty(BlockStateBasic.tierProperty, ((TileEntityInductionCell)tile).tier.getBaseTier());
		}
		
		if(tile instanceof TileEntityInductionProvider)
		{
			state = state.withProperty(BlockStateBasic.tierProperty, ((TileEntityInductionProvider)tile).tier.getBaseTier());
		}
		
		if(tile instanceof TileEntityInductionPort)
		{
			state = state.withProperty(BlockStateBasic.activeProperty, ((TileEntityInductionPort)tile).mode);
		}
		
		if(tile instanceof TileEntitySuperheatingElement)
		{
			TileEntitySuperheatingElement element = (TileEntitySuperheatingElement)tile;
			boolean active = false;
			
			if(element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null)
			{
				active = SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID);
			}
			
			state = state.withProperty(BlockStateBasic.activeProperty, active);
		}
		
		return state;
	}
	
	@SideOnly(Side.CLIENT)
    @Override
    public IBlockState getExtendedState(IBlockState stateIn, IBlockAccess w, BlockPos pos) 
	{
        if(stateIn.getBlock() == null || stateIn.getBlock().getMaterial() == Material.air) 
        {
            return stateIn;
        }
        
        IExtendedBlockState state = (IExtendedBlockState)stateIn;
        CTMBlockRenderContext ctx = new CTMBlockRenderContext(w, pos);

        return state.withProperty(ctmProperty, ctx);
    }

/*
	@Override
	public IIcon getIcon(ItemStack stack, int side)
	{
		if(BasicType.get(stack) == BasicType.BIN)
		{
			return binIcons[((ItemBlockBasic)stack.getItem()).getBaseTier(stack).ordinal()][side];
		}
		else if(BasicType.get(stack) == BasicType.INDUCTION_CELL)
		{
			return icons[3][((ItemBlockBasic)stack.getItem()).getBaseTier(stack).ordinal()];
		}
		else if(BasicType.get(stack) == BasicType.INDUCTION_PROVIDER)
		{
			return icons[4][((ItemBlockBasic)stack.getItem()).getBaseTier(stack).ordinal()];
		}
		
		return getIcon(side, stack.getItemDamage());
	}
*/

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);

			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).doUpdate();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).doUpdate();
			}
		}
	}

	public void initCTMs()
	{
		switch(getBasicBlock())
		{
			case BASIC_BLOCK_1:
				ctmData[7][0] = new CTMData(BasicBlockType.TELEPORTER_FRAME, MachineType.TELEPORTER);
				ctmData[9][0] = new CTMData(BasicBlockType.DYNAMIC_TANK, BasicBlockType.DYNAMIC_VALVE);
				ctmData[10][0] = new CTMData(BasicBlockType.STRUCTURAL_GLASS);
				ctmData[11][0] = new CTMData(BasicBlockType.DYNAMIC_TANK, BasicBlockType.DYNAMIC_VALVE);

				ctmData[14][0] = new CTMData(BasicBlockType.THERMAL_EVAPORATION_BLOCK, BasicBlockType.THERMAL_EVAPORATION_VALVE, BasicBlockType.THERMAL_EVAPORATION_CONTROLLER);
				ctmData[14][1] = new CTMData(BasicBlockType.THERMAL_EVAPORATION_BLOCK, BasicBlockType.THERMAL_EVAPORATION_VALVE, BasicBlockType.THERMAL_EVAPORATION_CONTROLLER);
				ctmData[15][0] = new CTMData(BasicBlockType.THERMAL_EVAPORATION_BLOCK, BasicBlockType.THERMAL_EVAPORATION_VALVE, BasicBlockType.THERMAL_EVAPORATION_CONTROLLER);

				break;
			case BASIC_BLOCK_2:
				ctmData[0][0] = new CTMData(BasicBlockType.THERMAL_EVAPORATION_BLOCK, BasicBlockType.THERMAL_EVAPORATION_VALVE, BasicBlockType.THERMAL_EVAPORATION_CONTROLLER);
				ctmData[1][0] = new CTMData(BasicBlockType.INDUCTION_CASING, BasicBlockType.INDUCTION_PORT);
				ctmData[2][0] = new CTMData(BasicBlockType.INDUCTION_CASING, BasicBlockType.INDUCTION_PORT);
				ctmData[2][1] = new CTMData(BasicBlockType.INDUCTION_CASING, BasicBlockType.INDUCTION_PORT);
				ctmData[3][0] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[3][1] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[3][2] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[3][3] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[4][0] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[4][1] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[4][2] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[4][3] = new CTMData(BasicBlockType.INDUCTION_CELL, BasicBlockType.INDUCTION_PROVIDER).setRenderConvexConnections();
				ctmData[5][0] = new CTMData(BasicBlockType.SUPERHEATING_ELEMENT).setRenderConvexConnections();
				ctmData[5][1] = new CTMData(BasicBlockType.SUPERHEATING_ELEMENT).setRenderConvexConnections();
				ctmData[7][0] = new CTMData(BasicBlockType.BOILER_CASING, BasicBlockType.BOILER_VALVE);
				ctmData[8][0] = new CTMData(BasicBlockType.BOILER_CASING, BasicBlockType.BOILER_VALVE);

				break;
		}
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
	{
		for(BasicBlockType type : BasicBlockType.values())
		{
			if(type.blockType == getBasicBlock())
			{
				switch(type)
				{
					case INDUCTION_CELL:
					case INDUCTION_PROVIDER:
					case BIN:
						for(BaseTier tier : BaseTier.values())
						{
							if(tier.isObtainable())
							{
								ItemStack stack = new ItemStack(item, 1, type.meta);
								((ItemBlockBasic)stack.getItem()).setBaseTier(stack, tier);
								list.add(stack);
							}
						}
						
						break;
					default:
						list.add(new ItemStack(item, 1, type.meta));
				}
			}
		}
	}

	@Override
	public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, SpawnPlacementType type)
	{
		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);

		switch(getBasicBlock())
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 10:
						return false;
					case 9:
					case 11:
						TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getTileEntity(pos);

						if(tileEntity != null)
						{
							if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
							{
								if(tileEntity.structure != null)
								{
									return false;
								}
							} 
							else {
								if(tileEntity.clientHasStructure)
								{
									return false;
								}
							}
						}
					default:
						return super.canCreatureSpawn(world, pos, type);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					case 1:
					case 2:
					case 7:
					case 8:
						TileEntityMultiblock tileEntity = (TileEntityMultiblock)world.getTileEntity(pos);

						if(tileEntity != null)
						{
							if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
							{
								if(tileEntity.structure != null)
								{
									return false;
								}
							} 
							else {
								if(tileEntity.clientHasStructure)
								{
									return false;
								}
							}
						}
					default:
						return super.canCreatureSpawn(world, pos, type);
				}
			default:
				return super.canCreatureSpawn(world, pos, type);
		}
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	{
		BasicBlockType type = BasicBlockType.get(world.getBlockState(pos));

		if(!world.isRemote && type == BasicBlockType.BIN)
		{
			TileEntityBin bin = (TileEntityBin)world.getTileEntity(pos);
			MovingObjectPosition mop = MekanismUtils.rayTrace(world, player);

			if(mop != null && mop.sideHit == bin.facing)
			{
				if(bin.bottomStack != null)
				{
					if(!player.isSneaking())
					{
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.removeStack().copy()));
					}
					else {
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.remove(1).copy()));
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BasicBlockType type = BasicBlockType.get(state);
		TileEntity tile = world.getTileEntity(pos);

		if(type == BasicBlockType.REFINED_OBSIDIAN)
		{
			if(entityplayer.isSneaking())
			{
				entityplayer.openGui(Mekanism.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}

		if(tile instanceof TileEntityThermalEvaporationController)
		{
			if(!entityplayer.isSneaking())
			{
				if(!world.isRemote)
				{
					entityplayer.openGui(Mekanism.instance, 33, world, pos.getX(), pos.getY(), pos.getZ());
				}
				
				return true;
			}
		}
		else if(tile instanceof TileEntitySecurityDesk)
		{
			String owner = ((TileEntitySecurityDesk)tile).owner;
			
			if(!entityplayer.isSneaking())
			{
				if(!world.isRemote)
				{
					if(owner == null || entityplayer.getName().equals(owner))
					{
						entityplayer.openGui(Mekanism.instance, 57, world, pos.getX(), pos.getY(), pos.getZ());
					}
					else {
						SecurityUtils.displayNoAccess(entityplayer);
					}
				}
				
				return true;
			}
		}
		else if(tile instanceof TileEntityBin)
		{
			TileEntityBin bin = (TileEntityBin)tile;

			if(entityplayer.getCurrentEquippedItem() != null && MekanismUtils.hasUsableWrench(entityplayer, pos))
			{
				if(!world.isRemote)
				{
					Item tool = entityplayer.getCurrentEquippedItem().getItem();
					
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, pos, false);
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, pos);
					}
	
					int change = bin.facing.rotateY().ordinal();
	
					bin.setFacing((short)change);
					world.notifyNeighborsOfStateChange(pos, this);
				}
				
				return true;
			}

			if(!world.isRemote)
			{
				if(bin.getItemCount() < bin.tier.storage)
				{
					if(bin.addTicks == 0 && entityplayer.getCurrentEquippedItem() != null)
					{
						if(entityplayer.getCurrentEquippedItem() != null)
						{
							ItemStack remain = bin.add(entityplayer.getCurrentEquippedItem());
							entityplayer.setCurrentItemOrArmor(0, remain);
							bin.addTicks = 5;
						}
					}
					else if(bin.addTicks > 0 && bin.getItemCount() > 0)
					{
						ItemStack[] inv = entityplayer.inventory.mainInventory;
	
						for(int i = 0; i < inv.length; i++)
						{
							if(bin.getItemCount() == bin.tier.storage)
							{
								break;
							}
	
							if(inv[i] != null)
							{
								ItemStack remain = bin.add(inv[i]);
								inv[i] = remain;
								bin.addTicks = 5;
							}
	
							((EntityPlayerMP)entityplayer).sendContainerToPlayer(entityplayer.openContainer);
						}
					}
				}
			}

			return true;
		}
		else if(tile instanceof IMultiblock)
		{
			if(world.isRemote)
			{
				return true;
			}
			
			return ((IMultiblock)world.getTileEntity(pos)).onActivate(entityplayer);
		}
		else if(tile instanceof IStructuralMultiblock)
		{
			if(world.isRemote)
			{
				return true;
			}
			
			return ((IStructuralMultiblock)world.getTileEntity(pos)).onActivate(entityplayer);
		}

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return BasicBlockType.get(world.getBlockState(pos)) != BasicBlockType.STRUCTURAL_GLASS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	public static boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null && tileEntity.structure != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.structure.fluidStored, itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

								if(tileEntity.structure.fluidStored.amount == 0)
								{
									tileEntity.structure.fluidStored = null;
								}

								return true;
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;

							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int max = tileEntity.structure.volume*TankUpdateProtocol.FLUID_PER_TANK;

				if(tileEntity.structure.fluidStored == null || (tileEntity.structure.fluidStored.isFluidEqual(itemFluid) && (tileEntity.structure.fluidStored.amount+itemFluid.amount <= max)))
				{
					boolean filled = false;
					
					if(player.capabilities.isCreativeMode)
					{
						filled = true;
					}
					else {
						ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
	
						if(containerItem != null)
						{
							if(itemStack.stackSize == 1)
							{
								player.setCurrentItemOrArmor(0, containerItem);
								filled = true;
							}
							else {
								if(player.inventory.addItemStackToInventory(containerItem))
								{
									itemStack.stackSize--;
	
									filled = true;
								}
							}
						}
						else {
							itemStack.stackSize--;
	
							if(itemStack.stackSize == 0)
							{
								player.setCurrentItemOrArmor(0, null);
							}
	
							filled = true;
						}
					}

					if(filled)
					{
						if(tileEntity.structure.fluidStored == null)
						{
							tileEntity.structure.fluidStored = itemFluid;
						}
						else {
							tileEntity.structure.fluidStored.amount += itemFluid.amount;
						}
						
						return true;
					}
				}
			}
		}

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
		return 3;
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		int metadata = state.getBlock().getMetaFromState(state);

		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		if(getBasicBlock() == BasicBlock.BASIC_BLOCK_1)
		{
			switch(metadata)
			{
				case 2:
					return 8;
				case 4:
					return 15;
				case 7:
					return 12;
			}
		}
		else if(getBasicBlock() == BasicBlock.BASIC_BLOCK_2)
		{
			if(metadata == 5 && tileEntity instanceof TileEntitySuperheatingElement)
			{
				TileEntitySuperheatingElement element = (TileEntitySuperheatingElement)tileEntity;
				
				if(element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null)
				{
					return SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 15 : 0;
				}
				
				return 0;
			}
		}

		return 0;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		BasicBlockType type = BasicBlockType.get(state);
		
		return type != null && type.tileEntityClass != null;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if(BasicBlockType.get(state) == null)
		{
			return null;
		}
		
		return BasicBlockType.get(state).create();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(world.getTileEntity(pos) instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
			int side = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			int height = Math.round(placer.rotationPitch);
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
			
			if(tileEntity instanceof TileEntitySecurityDesk)
			{
				((TileEntitySecurityDesk)tileEntity).owner = placer.getName();
			}

			if(tileEntity instanceof IBoundingBlock)
			{
				((IBoundingBlock)tileEntity).onPlace();
			}
		}

		world.markBlockRangeForRenderUpdate(pos, pos.add(1,1,1));
		world.checkLightFor(EnumSkyBlock.BLOCK, pos);
	    world.checkLightFor(EnumSkyBlock.SKY, pos);

		if(!world.isRemote && world.getTileEntity(pos) != null)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).doUpdate();
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).doUpdate();
			}
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, pos, state);
		
		world.removeTileEntity(pos);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		IBlockState state = world.getBlockState(pos);
		BasicBlockType type = BasicBlockType.get(state);
		ItemStack ret = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

		if(type == BasicBlockType.BIN)
		{
			TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(pos);
			InventoryBin inv = new InventoryBin(ret);

			((ITierItem)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
			inv.setItemCount(tileEntity.getItemCount());
			
			if(tileEntity.getItemCount() > 0)
			{
				inv.setItemType(tileEntity.itemType);
			}
		}
		else if(type == BasicBlockType.INDUCTION_CELL)
		{
			TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(pos);
			((ItemBlockBasic)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
		}
		else if(type == BasicBlockType.INDUCTION_PROVIDER)
		{
			TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(pos);
			((ItemBlockBasic)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
		}
		
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if(tileEntity instanceof IStrictEnergyStorage)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)ret.getItem();
			energizedItem.setEnergy(ret, ((IStrictEnergyStorage)tileEntity).getEnergy());
		}

		return ret;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return null;
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(null, world, pos, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(pos);
	}

	public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, pos, null);

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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		BlockPos offsetPos = pos.offset(side.getOpposite());
		
		if(BasicBlockType.get(world.getBlockState(offsetPos)) == BasicBlockType.STRUCTURAL_GLASS)
		{
			return ctmData[10][0].shouldRenderSide(world, pos, side);
		}
		else {
			return super.shouldSideBeRendered(world, pos, side);
		}
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
		return ctmData[getMetaFromState(state)][0];
	}
	
	@Override
	public String getOverrideTexture(IBlockState state, EnumFacing side)
	{
		BasicBlockType type = state.getValue(getBasicBlock().getProperty());
		
		if(type == BasicBlockType.INDUCTION_CELL || type == BasicBlockType.INDUCTION_PROVIDER)
		{
			return type.getName() + "_" + state.getValue(BlockStateBasic.tierProperty).getName();
		}
		
		if(type == BasicBlockType.THERMAL_EVAPORATION_CONTROLLER)
		{
			if(side == state.getValue(BlockStateFacing.facingProperty))
			{
				System.out.println("Yeet");
				return type.getName() + (state.getValue(BlockStateBasic.activeProperty) ? "_on" : "");
			}
		}
		
		if(type == BasicBlockType.INDUCTION_PORT)
		{
			return type.getName() + (state.getValue(BlockStateBasic.activeProperty) ? "output" : "input");
		}
		
		return null;
	}
	
	@Override
	public PropertyEnum<BasicBlockType> getTypeProperty()
	{
		return getBasicBlock().getProperty();
	}
}