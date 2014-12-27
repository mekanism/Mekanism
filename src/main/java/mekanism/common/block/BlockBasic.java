package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.CTMData;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockCTM;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntitySalinationBlock;
import mekanism.common.tile.TileEntitySalinationController;
import mekanism.common.tile.TileEntitySalinationValve;
import mekanism.common.util.MekanismUtils;

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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
 * 0:10: Dynamic Glass
 * 0:11: Dynamic Valve
 * 0:12: Copper Block
 * 0:13: Tin Block
 * 0:14: Salination Controller
 * 0:15: Salination Valve
 * 1:0: Salination Block
 * @author AidanBrady
 *
 */
public abstract class BlockBasic extends Block implements IBlockCTM
{
	public TextureAtlasSprite[][] icons = new TextureAtlasSprite[16][6];

	public CTMData[][] ctms = new CTMData[16][2];

	public BlockBasic()
	{
		super(Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	public abstract BasicBlock getBasicBlock();

	public PropertyEnum getProperty()
	{
		return getBasicBlock().predicatedProperty;
	}

	@Override
	public void onNeighborChange(IBlockAccess worldIn, BlockPos pos, BlockPos neighbor)
	{
		if(!(worldIn instanceof World && ((World)worldIn).isRemote))
		{
			IBlockState state = worldIn.getBlockState(pos);
			
			TileEntity tileEntity = worldIn.getTileEntity(pos);

			if(state.getBlock() == this && tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(state.getBlock());
			}
		}
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap register)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				ctms[9][0] = new CTMData("ctm/DynamicTank", this, Arrays.asList(9, 11)).registerIcons(register);
				ctms[10][0] = new CTMData("ctm/DynamicGlass", this, Arrays.asList(10)).registerIcons(register);
				ctms[11][0] = new CTMData("ctm/DynamicValve", this, Arrays.asList(11, 9)).registerIcons(register);

				ctms[14][0] = new CTMData("ctm/SalinationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/SalinationController").registerIcons(register);
				ctms[14][1] = new CTMData("ctm/SalinationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/SalinationControllerOn").registerIcons(register);
				ctms[15][0] = new CTMData("ctm/SalinationValve", this, Arrays.asList(15, 14)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).registerIcons(register);

				icons[0][0] = register.registerIcon("mekanism:OsmiumBlock");
				icons[1][0] = register.registerIcon("mekanism:BronzeBlock");
				icons[2][0] = register.registerIcon("mekanism:RefinedObsidian");
				icons[3][0] = register.registerIcon("mekanism:CoalBlock");
				icons[4][0] = register.registerIcon("mekanism:RefinedGlowstone");
				icons[5][0] = register.registerIcon("mekanism:SteelBlock");
				icons[6][0] = register.registerIcon("mekanism:BinSide");
				icons[6][1] = register.registerIcon("mekanism:BinTop");
				icons[6][2] = register.registerIcon("mekanism:BinFront");
				icons[6][3] = register.registerIcon("mekanism:BinTopOn");
				icons[6][4] = register.registerIcon("mekanism:BinFrontOn");
				icons[7][0] = register.registerIcon("mekanism:TeleporterFrame");
				icons[8][0] = register.registerIcon("mekanism:SteelCasing");
				icons[9][0] = ctms[9][0].mainTextureData.icon;
				icons[10][0] = ctms[10][0].mainTextureData.icon;
				icons[11][0] = ctms[11][0].mainTextureData.icon;
				icons[12][0] = register.registerIcon("mekanism:CopperBlock");
				icons[13][0] = register.registerIcon("mekanism:TinBlock");
				icons[14][0] = ctms[14][0].facingOverride.icon;
				icons[14][1] = ctms[14][1].facingOverride.icon;
				icons[14][2] = ctms[14][0].mainTextureData.icon;
				icons[15][0] = ctms[15][0].mainTextureData.icon;
				break;
			case BASIC_BLOCK_2:
				ctms[0][0] = new CTMData("ctm/SalinationBlock", this, Arrays.asList(0)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock, Arrays.asList(14, 15)).registerIcons(register);

				icons[0][0] = ctms[0][0].mainTextureData.icon;
				break;
		}
	}
*/

/*
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{
		int meta = world.getBlockMetadata(x, y, z);

		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 6:
						TileEntityBasicBlock tileEntity6 = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

						if(side == 0 || side == 1)
						{
							return MekanismUtils.isActive(worldIn, x, y, z) ? icons[meta][3] : icons[meta][1];
						} else if(side == tileEntity6.facing)
						{
							return MekanismUtils.isActive(worldIn, x, y, z) ? icons[meta][4] : icons[meta][2];
						} else
						{
							return icons[meta][0];
						}
					case 9:
					case 10:
					case 11:
						return ctms[meta][0].getIcon(side);
					case 14:
						TileEntitySalinationController tileEntity14 = (TileEntitySalinationController)world.getTileEntity(new BlockPos(x, y, z));

						if(side == tileEntity14.facing)
						{
							return MekanismUtils.isActive(worldIn, x, y, z) ? icons[meta][1] : icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					default:
						return getIcon(side, meta);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					default:
						return getIcon(side, meta);
				}
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(EnumFacing side, int meta)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 6:
						if(side == 0 || side == 1)
						{
							return icons[meta][1];
						} else if(side == 3)
						{
							return icons[meta][2];
						} else
						{
							return icons[meta][0];
						}
					case 14:
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
			case BASIC_BLOCK_2:
				return icons[meta][0];
			default:
				return icons[meta][0];
		}
	}
*/

	@Override
	public int damageDropped(IBlockState state)
	{
		return this.getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(BasicBlockType type: BasicBlockType.values())
		{
			if(type.blockType == getBasicBlock())
				list.add(new ItemStack(item, 1, type.meta));
		}
	}

	@Override
	public boolean canCreatureSpawn(IBlockAccess worldIn, BlockPos pos, SpawnPlacementType type)
	{
		IBlockState state = worldIn.getBlockState(pos);
		if(state.getBlock() != this)
		{
			return true;
		}
		BasicBlockType blockType = (BasicBlockType)state.getValue(getProperty());
		
		switch(blockType)
		{
				case DYNAMIC_GLASS:
				case DYNAMIC_TANK:
				case DYNAMIC_VALVE:
					TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)worldIn.getTileEntity(pos);

					if(tileEntity != null)
					{
						if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
						{
							if(tileEntity.structure != null)
							{
								return false;
							}
						} else
						{
							if(tileEntity.clientHasStructure)
							{
								return false;
							}
						}
					}
			default:
				return super.canCreatureSpawn(worldIn, pos, type);
		}
	}

	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
	{
		IBlockState state = worldIn.getBlockState(pos);

		if(state.getValue(getProperty()) == BasicBlockType.BIN)
		{
			TileEntityBin bin = (TileEntityBin)worldIn.getTileEntity(pos);
			MovingObjectPosition mop = MekanismUtils.rayTrace(worldIn, playerIn);

			if(mop != null && mop.sideHit == bin.getFacing())
			{
				if(bin.bottomStack != null)
				{
					if(!playerIn.isSneaking())
					{
						worldIn.spawnEntityInWorld(new EntityItem(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, bin.removeStack().copy()));
					} else
					{
						worldIn.spawnEntityInWorld(new EntityItem(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, bin.remove(1).copy()));
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BasicBlockType type = (BasicBlockType)state.getValue(getProperty());
		
		if(type != BasicBlockType.BIN)
		{
			if(ItemAttacher.canAttach(playerIn.getCurrentEquippedItem()))
			{
				return false;
			}
		}

		if(type == BasicBlockType.REFINED_OBSIDIAN)
		{
			if(playerIn.isSneaking())
			{
				playerIn.openGui(Mekanism.instance, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}

		if(type == BasicBlockType.SALINATION_CONTROLLER)
		{
			if(!playerIn.isSneaking())
			{
				playerIn.openGui(Mekanism.instance, 33, worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}

		if(worldIn.isRemote)
		{
			return true;
		}

		if(type == BasicBlockType.BIN)
		{
			TileEntityBin bin = (TileEntityBin)worldIn.getTileEntity(pos);

			if(bin.getItemCount() < bin.MAX_STORAGE)
			{
				if(bin.addTicks == 0 && playerIn.getCurrentEquippedItem() != null)
				{
					if(playerIn.getCurrentEquippedItem() != null)
					{
						ItemStack remain = bin.add(playerIn.getCurrentEquippedItem());
						playerIn.setCurrentItemOrArmor(0, remain);
						bin.addTicks = 5;
					}
				}
				else if(bin.addTicks > 0 && bin.getItemCount() > 0)
				{
					ItemStack[] inv = playerIn.inventory.mainInventory;

					for(int i = 0; i < inv.length; i++)
					{
						if(bin.getItemCount() == bin.MAX_STORAGE)
						{
							break;
						}

						if(inv[i] != null)
						{
							ItemStack remain = bin.add(inv[i]);
							inv[i] = remain;
							bin.addTicks = 5;
						}

						((EntityPlayerMP)playerIn).sendContainerAndContentsToPlayer(playerIn.openContainer, playerIn.openContainer.getInventory());
					}
				}
			}

			return true;
		}
		else if(type == BasicBlockType.DYNAMIC_GLASS || type == BasicBlockType.DYNAMIC_TANK || type == BasicBlockType.DYNAMIC_VALVE)
		{
			if(!playerIn.isSneaking() && ((TileEntityDynamicTank)worldIn.getTileEntity(pos)).structure != null)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)worldIn.getTileEntity(pos);

				if(!manageInventory(playerIn, tileEntity))
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
					playerIn.openGui(Mekanism.instance, 18, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
				else {
					playerIn.inventory.markDirty();
					tileEntity.sendPacketToRenderer();
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{

		return worldIn.getBlockState(pos).getValue(getProperty()) != BasicBlockType.DYNAMIC_GLASS;
	}

	private boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity)
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
	public int getLightValue(IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		IBlockState state = worldIn.getBlockState(pos);

		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		BasicBlockType type = (BasicBlockType)state.getValue(getProperty());

		switch(type)
		{
			case REFINED_OBSIDIAN:
				return 8;
			case REFINED_GLOWSTONE:
				return 15;
			case TELEPORTER_FRAME:
				return 12;
		}

		return 0;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		BasicBlockType type = (BasicBlockType)state.getValue(getProperty());
		switch(type)
		{
			case BIN:
			case DYNAMIC_GLASS:
			case DYNAMIC_TANK:
			case DYNAMIC_VALVE:
			case COPPER_BLOCK:
			case SALINATION_BLOCK:
			case SALINATION_CONTROLLER:
			case SALINATION_VALVE:
				return true;
			default:
				return false;
		}
	}

	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state)
	{
		BasicBlockType type = (BasicBlockType)state.getValue(getProperty());
		switch(type)
		{
			case BIN:
				return new TileEntityBin();
			case DYNAMIC_GLASS:
			case DYNAMIC_TANK:
				return new TileEntityDynamicTank();
			case DYNAMIC_VALVE:
				return new TileEntityDynamicValve();
			case SALINATION_CONTROLLER:
				return new TileEntitySalinationController();
			case SALINATION_VALVE:
				return new TileEntitySalinationValve();
			case SALINATION_BLOCK:
				return new TileEntitySalinationBlock();
			default:
				return null;
		}

	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(worldIn.getTileEntity(pos) instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);
			int height = Math.round(placer.rotationPitch);
			EnumFacing newFacing;

			if(tileEntity == null)
			{
				return;
			}

			if(height >= 65 && tileEntity.canSetFacing(EnumFacing.DOWN))
			{
				newFacing = EnumFacing.DOWN;
			}
			else if(height <= -65 && tileEntity.canSetFacing(EnumFacing.UP))
			{
				newFacing = EnumFacing.UP;
			}
			else
			{
				newFacing = placer.getHorizontalFacing().getOpposite();
			}

			if(tileEntity.canSetFacing(newFacing))
			{
				tileEntity.setFacing(newFacing);
			}

			tileEntity.redstone = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;

			if(tileEntity instanceof IBoundingBlock)
			{
				((IBoundingBlock)tileEntity).onPlace();
			}
		}

		worldIn.markBlockRangeForRenderUpdate(pos, pos);
		worldIn.checkLightFor(EnumSkyBlock.BLOCK, pos);
		worldIn.checkLightFor(EnumSkyBlock.SKY, pos);

		if(!worldIn.isRemote && worldIn.getTileEntity(pos) != null)
		{
			TileEntity tileEntity = worldIn.getTileEntity(pos);

			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World worldIn, BlockPos pos)
	{
		IBlockState state = worldIn.getBlockState(pos);
		ItemStack ret = new ItemStack(this, 1, state.getBlock().getMetaFromState(state));

		if(ret.getItemDamage() == 6)
		{
			TileEntityBin tileEntity = (TileEntityBin)worldIn.getTileEntity(pos);
			InventoryBin inv = new InventoryBin(ret);

			inv.setItemCount(tileEntity.getItemCount());

			if(tileEntity.getItemCount() > 0)
			{
				inv.setItemType(tileEntity.itemType);
			}
		}

		return ret;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return null;
	}

	@Override
	public boolean removedByPlayer(World worldIn, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		IBlockState state = worldIn.getBlockState(pos);
		if(!player.capabilities.isCreativeMode && !worldIn.isRemote && canHarvestBlock(worldIn, pos, player))
		{

			float motion = 0.7F;
			double motionX = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(worldIn, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, getPickBlock(null, worldIn, pos));

			worldIn.spawnEntityInWorld(entityItem);
		}

		return worldIn.setBlockToAir(pos);
	}

	public ItemStack dismantleBlock(World worldIn, BlockPos pos, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, worldIn, pos);

		worldIn.setBlockToAir(pos);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(worldIn, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

			worldIn.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{
		IBlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if(state.getValue(getProperty()) == BasicBlockType.DYNAMIC_GLASS)
		{
			return ctms[10][0].shouldRenderSide(worldIn, pos, side);
		}
		else {
			return super.shouldSideBeRendered(worldIn, pos, side);
		}
	}

	@Override
	public CTMData getCTMData(IBlockAccess worldIn, BlockPos pos, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(worldIn, pos))
		{
			return ctms[meta][1];
		}

		return ctms[meta][0];
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockStateBasic(this, getProperty());
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		BasicBlockType type = BasicBlockType.getBlockType(getBasicBlock(), meta&0xF);

		return this.getDefaultState().withProperty(getProperty(), type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		BasicBlockType type = (BasicBlockType)state.getValue(getProperty());

		return type.meta;
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
				if(basicTile.canSetFacing(dir))
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

			if(basicTile.canSetFacing(axis))
			{
				basicTile.setFacing(axis);
				return true;
			}
		}

		return false;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile instanceof TileEntityBasicBlock)
		{
			return state.withProperty(BlockStateFacing.facingProperty, ((TileEntityBasicBlock)tile).getFacing());
		}
		return state;
	}
}