package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IElectricChest;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IInvConfiguration;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Block class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * 1:9: Seismic Vibrator
 * 1:10: Pressurized Reaction Chamber
 * 1:11: Portable Tank
 * 1:12: Fluidic Plenisher
 * 1:13: Laser
 * 1:14: Laser Amplifier
 * 1:15: Laser Tractor Beam
 * 2:0: Entangled Block
 * 2:1: Ambient Accumulator
 * @author AidanBrady
 *
 */
@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
public class BlockMachine extends BlockContainer implements ISpecialBounds, IPeripheralProvider
{
	public TextureAtlasSprite[][] icons = new TextureAtlasSprite[16][16];

	public BlockMachine()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap register)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				icons[0][0] = register.registerIcon("mekanism:EnrichmentChamberFrontOff");
				icons[0][1] = register.registerIcon("mekanism:EnrichmentChamberFrontOn");
				icons[0][2] = register.registerIcon("mekanism:SteelCasing");
				icons[1][0] = register.registerIcon("mekanism:OsmiumCompressorFrontOff");
				icons[1][1] = register.registerIcon("mekanism:OsmiumCompressorFrontOn");
				icons[1][2] = register.registerIcon("mekanism:SteelCasing");
				icons[2][0] = register.registerIcon("mekanism:CombinerFrontOff");
				icons[2][1] = register.registerIcon("mekanism:CombinerFrontOn");
				icons[2][2] = register.registerIcon("mekanism:SteelCasing");
				icons[3][0] = register.registerIcon("mekanism:CrusherFrontOff");
				icons[3][1] = register.registerIcon("mekanism:CrusherFrontOn");
				icons[3][2] = register.registerIcon("mekanism:SteelCasing");
				icons[5][0] = register.registerIcon("mekanism:BasicFactoryFront");
				icons[5][1] = register.registerIcon("mekanism:BasicFactorySide");
				icons[5][2] = register.registerIcon("mekanism:BasicFactoryTop");
				icons[6][0] = register.registerIcon("mekanism:AdvancedFactoryFront");
				icons[6][1] = register.registerIcon("mekanism:AdvancedFactorySide");
				icons[6][2] = register.registerIcon("mekanism:AdvancedFactoryTop");
				icons[7][0] = register.registerIcon("mekanism:EliteFactoryFront");
				icons[7][1] = register.registerIcon("mekanism:EliteFactorySide");
				icons[7][2] = register.registerIcon("mekanism:EliteFactoryTop");
				icons[9][0] = register.registerIcon("mekanism:PurificationChamberFrontOff");
				icons[9][1] = register.registerIcon("mekanism:PurificationChamberFrontOn");
				icons[9][2] = register.registerIcon("mekanism:SteelCasing");
				icons[10][0] = register.registerIcon("mekanism:EnergizedSmelterFrontOff");
				icons[10][1] = register.registerIcon("mekanism:EnergizedSmelterFrontOn");
				icons[10][2] = register.registerIcon("mekanism:SteelCasing");
				icons[11][0] = register.registerIcon("mekanism:Teleporter");
				break;
			case MACHINE_BLOCK_2:
				icons[3][0] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOff");
				icons[3][1] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOn");
				icons[3][2] = register.registerIcon("mekanism:SteelCasing");
				icons[5][0] = register.registerIcon("mekanism:PrecisionSawmillFrontOff");
				icons[5][1] = register.registerIcon("mekanism:PrecisionSawmillFrontOn");
				icons[5][2] = register.registerIcon("mekanism:SteelCasing");
				break;
			case MACHINE_BLOCK_3:
				icons[0][0] = register.registerIcon("mekanism:AmbientAccumulator");
				icons[1][0] = register.registerIcon("mekanism:SteelCasing");
				icons[2][0] = register.registerIcon("mekanism:GasCentrifuge");
				break;
		}

	}
*/

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);
		int height = Math.round(placer.rotationPitch);
		EnumFacing newFacing;

		if(tileEntity == null)
		{
			return;
		}

		if(height >= 65)
		{
			newFacing = EnumFacing.DOWN;
		}
		else if(height <= -65)
		{
			newFacing = EnumFacing.UP;
		}
		else
		{
			newFacing = placer.getHorizontalFacing().getOpposite();
		}
		
		if(tileEntity instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter)tileEntity;

			if(!transporter.hasInventory())
			{
				for(EnumFacing dir : EnumFacing.values())
				{
					TileEntity tile = Coord4D.get(transporter).offset(dir).getTileEntity(worldIn);

					if(tile instanceof IInventory)
					{
						newFacing = dir.getOpposite();
						break;
					}
				}
			}
		}


		worldIn.setBlockState(pos, state.withProperty(BlockStateFacing.facingProperty, newFacing));

		tileEntity.redstone = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);

		if(MekanismUtils.isActive(worldIn, pos) && ((IActiveState)tileEntity).renderUpdate() && client.machineEffects)
		{
			float xRandom = (float)pos.getX() + 0.5F;
			float yRandom = (float)pos.getY() + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)pos.getZ() + 0.5F;
			float iRandom = 0.52F;
			float jRandom = rand.nextFloat() * 0.6F - 0.3F;

			EnumFacing side = (EnumFacing)state.getValue(BlockStateFacing.facingProperty);

			if(tileEntity instanceof TileEntityMetallurgicInfuser)
			{
				side = side.getOpposite();
			}

			if(side == EnumFacing.WEST)
			{
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				worldIn.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == EnumFacing.EAST)
			{
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				worldIn.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == EnumFacing.NORTH)
			{
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
				worldIn.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == EnumFacing.SOUTH)
			{
				worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
				worldIn.spawnParticle(EnumParticleTypes.REDSTONE, (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if(client.machineEffects && tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		return 0;
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(EnumFacing side, int meta)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						if(side == 3)
						{
							return icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					case 5:
					case 6:
					case 7:
						if(side == 3)
						{
							return icons[meta][0];
						} else if(side == 0 || side == 1)
						{
							return icons[meta][2];
						} else
						{
							return icons[meta][1];
						}
					default:
						return icons[11][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						if(side == 3)
						{
							return icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					default:
						return icons[meta][0];
				}
			default:
				return null;
		}
	}
*/

/*
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						if(side == tileEntity.facing)
						{
							return MekanismUtils.isActive(world, x, y, z) ? icons[meta][1] : icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					case 5:
					case 6:
					case 7:
						if(side == tileEntity.facing)
						{
							return icons[meta][0];
						} else if(side == 0 || side == 1)
						{
							return icons[meta][2];
						} else
						{
							return icons[meta][1];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						if(side == tileEntity.facing)
						{
							return MekanismUtils.isActive(world, x, y, z) ? icons[meta][1] : icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					default:
						return icons[meta][0];
				}
		}
		return null;
	}
*/

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(MachineBlockType type : MachineBlockType.values())
		{
			switch(type)
			{
				case BASIC_FACTORY:
				case ADVANCED_FACTORY:
				case ELITE_FACTORY:
					for(RecipeType recipe : RecipeType.values())
					{
						ItemStack stack = new ItemStack(item, 1, type.ordinal());
						((IFactory)stack.getItem()).setRecipeType(recipe.ordinal(), stack);
						list.add(stack);
					}
					break;
				case PORTABLE_TANK:
					list.add(new ItemStack(item, 1, type.ordinal()));

					ItemBlockMachine itemMachine = (ItemBlockMachine)item;

					for(Fluid f : FluidRegistry.getRegisteredFluids().values())
					{
						ItemStack filled = new ItemStack(item, 1, type.ordinal());
						itemMachine.setFluidStack(new FluidStack(f, itemMachine.getCapacity(filled)), filled);
						itemMachine.setPrevScale(filled, 1);
						list.add(filled);
					}
					break;
				default:
					list.add(new ItemStack(item, 1, type.ordinal()));
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(ItemAttacher.canAttach(playerIn.getCurrentEquippedItem()))
		{
			return false;
		}

		if(worldIn.isRemote)
		{
			return true;
		}

		MachineBlockType type = (MachineBlockType)state.getValue(BlockStateMachine.typeProperty);

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);

		if(playerIn.getCurrentEquippedItem() != null)
		{
			Item tool = playerIn.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(playerIn, pos))
			{
				if(playerIn.isSneaking() && type != MachineBlockType.ELECTRIC_CHEST)
				{
					dismantleBlock(worldIn, pos, false);
					return true;
				}

				if(ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") && tool instanceof IToolWrench)
					((IToolWrench)tool).wrenchUsed(playerIn, pos);

				rotateBlock(worldIn, pos, side);
				return true;
			}
		}

		if(tileEntity != null)
		{
			switch(type)
			{
				case ELECTRIC_CHEST:
					TileEntityElectricChest electricChest = (TileEntityElectricChest)tileEntity;

					if(!(playerIn.isSneaking() || worldIn.isSideSolid(pos.up(), EnumFacing.DOWN)))
					{
						if(electricChest.canAccess())
						{
							MekanismUtils.openElectricChestGui((EntityPlayerMP)playerIn, electricChest, null, true);
						} else if(!electricChest.authenticated)
						{
							Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, true, false, 2, 0, null, Coord4D.get(electricChest)), (EntityPlayerMP)playerIn);
						} else
						{
							Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, true, false, 1, 0, null, Coord4D.get(electricChest)), (EntityPlayerMP)playerIn);
						}

						return true;
					}
					break;
				case PORTABLE_TANK:
					if(playerIn.getCurrentEquippedItem() != null && FluidContainerRegistry.isContainer(playerIn.getCurrentEquippedItem()))
					{
						if(manageInventory(playerIn, (TileEntityPortableTank)tileEntity))
						{
							playerIn.inventory.markDirty();
							return true;
						}
					} else
					{
						playerIn.openGui(Mekanism.instance, type.guiId, worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
					return true;
				case LOGISTICAL_SORTER:
					LogisticalSorterGuiMessage.openServerGui(SorterGuiPacket.SERVER, 0, worldIn, (EntityPlayerMP)playerIn, Coord4D.get(tileEntity), -1);
					return true;
				default:
					if(!playerIn.isSneaking() && type.guiId != -1)
					{
						playerIn.openGui(Mekanism.instance, type.guiId, worldIn, pos.getX(), pos.getY(), pos.getZ());
						return true;
					}
					return false;
			}
		}
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if(state.getValue(BlockStateMachine.typeProperty) == null)
		{
			return null;
		}

		return ((MachineBlockType)state.getValue(BlockStateMachine.typeProperty)).create();

	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return null;
	}

/*
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
*/

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
	{
		return null;
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return ClientProxy.MACHINE_RENDER_ID;
	}
*/

	@Override
	public float getBlockHardness(World world, BlockPos pos)
	{
		if(!(world.getBlockState(pos).getValue(BlockStateMachine.typeProperty) != MachineBlockType.ELECTRIC_CHEST))
		{
			return blockHardness;
		}
		else {
			TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getTileEntity(pos);
			return tileEntity.canAccess() ? 3.5F : -1;
		}
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(world, pos, player))
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(null, world, pos));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(pos);
	}
	
	private boolean manageInventory(EntityPlayer player, TileEntityPortableTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.fluidTank.getFluid() != null && tileEntity.fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.fluidTank.getFluid(), itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int needed = tileEntity.getCurrentNeeded();
				
				if((tileEntity.fluidTank.getFluid() == null && itemFluid.amount <= tileEntity.fluidTank.getCapacity()) || itemFluid.amount <= needed)
				{
					if(tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid))
					{
						return false;
					}
					
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
						int toFill = Math.min(tileEntity.fluidTank.getCapacity()-tileEntity.fluidTank.getFluidAmount(), itemFluid.amount);
						
						tileEntity.fluidTank.fill(itemFluid, true);
						
						if(itemFluid.amount-toFill > 0)
						{
							tileEntity.pushUp(new FluidStack(itemFluid.getFluid(), itemFluid.amount-toFill), true);
						}
						
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		if(!(world instanceof World && ((World)world).isRemote))
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(world.getBlockState(neighbor).getBlock());
			}

			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;

				if(!sorter.hasInventory())
				{
					for(EnumFacing dir : EnumFacing.values())
					{
						TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

						if(tile instanceof IInventory && world instanceof World)
						{
							rotateBlock((World)world, pos, dir.getOpposite());
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		ItemStack itemStack = new ItemStack(this, 1, world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)));

		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		if(tileEntity instanceof IUpgradeTile)
		{
			((IUpgradeTile)tileEntity).getComponent().write(itemStack.getTagCompound());
		}

		if(tileEntity instanceof IInvConfiguration)
		{
			IInvConfiguration config = (IInvConfiguration)tileEntity;

			itemStack.getTagCompound().setBoolean("hasSideData", true);

			itemStack.getTagCompound().setBoolean("ejecting", config.getEjector().isEjecting());

			for(int i = 0; i < 6; i++)
			{
				itemStack.getTagCompound().setByte("config"+i, config.getConfiguration()[i]);
			}
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(tileEntity instanceof IRedstoneControl)
		{
			IRedstoneControl control = (IRedstoneControl)tileEntity;
			itemStack.getTagCompound().setInteger("controlType", control.getControlType().ordinal());
		}

		if(tileEntity instanceof TileEntityElectricBlock)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
			energizedItem.setEnergy(itemStack, ((TileEntityElectricBlock)tileEntity).electricityStored);
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).inventory.length > 0)
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
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

		if(tileEntity instanceof TileEntityElectricChest)
		{
			IElectricChest electricChest = (IElectricChest)itemStack.getItem();
			electricChest.setAuthenticated(itemStack, ((TileEntityElectricChest)tileEntity).authenticated);
			electricChest.setLocked(itemStack, ((TileEntityElectricChest)tileEntity).locked);
			electricChest.setPassword(itemStack, ((TileEntityElectricChest)tileEntity).password);
		}

		if(tileEntity instanceof TileEntityFactory)
		{
			IFactory factoryItem = (IFactory)itemStack.getItem();
			factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType.ordinal(), itemStack);
		}

		return itemStack;
	}

	public ItemStack dismantleBlock(World world, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, pos);

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
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		MachineBlockType type = (MachineBlockType)world.getBlockState(pos).getValue(BlockStateMachine.typeProperty);

		switch(type)
		{
			case CHARGEPAD:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
				break;
			case PORTABLE_TANK:
				setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
				break;
			default:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		if(world.getTileEntity(pos) instanceof TileEntityChargepad)
		{
			return null;
		}

		return super.getCollisionBoundingBox(world, pos, state);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		MachineBlockType type = (MachineBlockType)world.getBlockState(pos).getValue(BlockStateMachine.typeProperty);

		switch(type)
		{
			case CHARGEPAD:
				return false;
			case PORTABLE_TANK:
				return side == EnumFacing.UP || side == EnumFacing.DOWN;
		}

		return true;
	}

	@Override
	public void setRenderBounds(Block block, int metadata) {}

	@Override
	public boolean doDefaultBoundSetting(int metadata)
	{
		return false;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		
		if(te != null && te instanceof IPeripheral)
		{
			return (IPeripheral)te;
		}
		
		return null;
	}

	@Override
	public EnumFacing[] getValidRotations(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		EnumFacing[] valid = new EnumFacing[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(EnumFacing dir : EnumFacing.values())
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
		return world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockStateFacing.facingProperty, axis));
	}
}
