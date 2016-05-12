package mekanism.common.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DefIcon;
import mekanism.client.render.MekanismRenderer.ICustomBlockIcon;
import mekanism.common.CTMData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockCTM;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ITierItem;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionPort;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityStructuralGlass;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
public class BlockBasic extends Block implements IBlockCTM, ICustomBlockIcon
{
	public IIcon[][] icons = new IIcon[16][16];
	public IIcon[][] binIcons = new IIcon[16][16];

	public CTMData[][] ctms = new CTMData[16][4];
	
	public static String ICON_BASE = "mekanism:SteelCasing";

	public BasicBlock blockType;

	public BlockBasic(BasicBlock type)
	{
		super(Material.iron);
		setHardness(5F);
		setResistance(20F);
		setCreativeTab(Mekanism.tabMekanism);
		blockType = type;
	}
	
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

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(block == this && tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).update();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).update();
			}
		}
	}
	
	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
		BasicType type = BasicType.get(this, world.getBlockMetadata(x, y, z));
		
		if(type == BasicType.REFINED_OBSIDIAN)
		{
			return 4000F;
		}
		
		return blockResistance;
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				ctms[7][0] = new CTMData("ctm/TeleporterFrame", this, Arrays.asList(7)).addOtherBlockConnectivities(MekanismBlocks.MachineBlock, Arrays.asList(11)).registerIcons(register);
				ctms[9][0] = new CTMData("ctm/DynamicTank", this, Arrays.asList(9, 11)).registerIcons(register);
				ctms[10][0] = new CTMData("ctm/StructuralGlass", this, Arrays.asList(10)).registerIcons(register);
				ctms[11][0] = new CTMData("ctm/DynamicValve", this, Arrays.asList(11, 9)).registerIcons(register);

				ctms[14][0] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/ThermalEvaporationController").registerIcons(register);
				ctms[14][1] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(14, 15)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).addFacingOverride("ctm/ThermalEvaporationControllerOn").registerIcons(register);
				ctms[15][0] = new CTMData("ctm/ThermalEvaporationValve", this, Arrays.asList(15, 14)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock2, Arrays.asList(0)).registerIcons(register);

				icons[0][0] = register.registerIcon("mekanism:OsmiumBlock");
				icons[1][0] = register.registerIcon("mekanism:BronzeBlock");
				icons[2][0] = register.registerIcon("mekanism:RefinedObsidian");
				icons[3][0] = register.registerIcon("mekanism:CoalBlock");
				icons[4][0] = register.registerIcon("mekanism:RefinedGlowstone");
				icons[5][0] = register.registerIcon("mekanism:SteelBlock");
				icons[6][0] = register.registerIcon(ICON_BASE);
				
				MekanismRenderer.loadDynamicTextures(register, "bin/BinBasic", binIcons[0], new DefIcon(register.registerIcon("mekanism:bin/BinBasicTop"), 0), new DefIcon(register.registerIcon("mekanism:bin/BinBasicTopOn"), 6));
				MekanismRenderer.loadDynamicTextures(register, "bin/BinAdvanced", binIcons[1], new DefIcon(register.registerIcon("mekanism:bin/BinAdvancedTop"), 0), new DefIcon(register.registerIcon("mekanism:bin/BinAdvancedTopOn"), 6));
				MekanismRenderer.loadDynamicTextures(register, "bin/BinElite", binIcons[2], new DefIcon(register.registerIcon("mekanism:bin/BinEliteTop"), 0), new DefIcon(register.registerIcon("mekanism:bin/BinEliteTopOn"), 6));
				MekanismRenderer.loadDynamicTextures(register, "bin/BinUltimate", binIcons[3], new DefIcon(register.registerIcon("mekanism:bin/BinUltimateTop"), 0), new DefIcon(register.registerIcon("mekanism:bin/BinUltimateTopOn"), 6));
				
				icons[7][0] = ctms[7][0].mainTextureData.icon;
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
				ctms[0][0] = new CTMData("ctm/ThermalEvaporationBlock", this, Arrays.asList(0)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock, Arrays.asList(14, 15)).registerIcons(register);
				ctms[1][0] = new CTMData("ctm/InductionCasing", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[2][0] = new CTMData("ctm/InductionPortInput", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[2][1] = new CTMData("ctm/InductionPortOutput", this, Arrays.asList(1, 2)).registerIcons(register);
				ctms[3][0] = new CTMData("ctm/InductionCellBasic", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][1] = new CTMData("ctm/InductionCellAdvanced", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][2] = new CTMData("ctm/InductionCellElite", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[3][3] = new CTMData("ctm/InductionCellUltimate", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][0] = new CTMData("ctm/InductionProviderBasic", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][1] = new CTMData("ctm/InductionProviderAdvanced", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][2] = new CTMData("ctm/InductionProviderElite", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[4][3] = new CTMData("ctm/InductionProviderUltimate", this, Arrays.asList(3, 4)).registerIcons(register).setRenderConvexConnections();
				ctms[5][0] = new CTMData("ctm/SuperheatingElement", this, Arrays.asList(5)).registerIcons(register).setRenderConvexConnections();
				ctms[5][1] = new CTMData("ctm/SuperheatingElementOn", this, Arrays.asList(5)).registerIcons(register).setRenderConvexConnections();
				ctms[7][0] = new CTMData("ctm/BoilerCasing", this, Arrays.asList(7, 8)).registerIcons(register);
				ctms[8][0] = new CTMData("ctm/BoilerValve", this, Arrays.asList(7, 8)).registerIcons(register);
				
				icons[6][0] = register.registerIcon("mekanism:PressureDisperser");
				
				icons[0][0] = ctms[0][0].mainTextureData.icon;
				icons[1][0] = ctms[1][0].mainTextureData.icon;
				icons[2][0] = ctms[2][0].mainTextureData.icon;
				icons[2][1] = ctms[2][1].mainTextureData.icon;
				icons[3][0] = ctms[3][0].mainTextureData.icon;
				icons[3][1] = ctms[3][1].mainTextureData.icon;
				icons[3][2] = ctms[3][2].mainTextureData.icon;
				icons[3][3] = ctms[3][3].mainTextureData.icon;
				icons[4][0] = ctms[4][0].mainTextureData.icon;
				icons[4][1] = ctms[4][1].mainTextureData.icon;
				icons[4][2] = ctms[4][2].mainTextureData.icon;
				icons[4][3] = ctms[4][3].mainTextureData.icon;
				icons[5][0] = ctms[5][0].mainTextureData.icon;
				icons[5][1] = ctms[5][1].mainTextureData.icon;
				icons[7][0] = ctms[7][0].mainTextureData.icon;
				icons[8][0] = ctms[8][0].mainTextureData.icon;
				
				icons[9][0] = register.registerIcon(ICON_BASE);
				
				break;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);

		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 6:
						TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);

						boolean active = MekanismUtils.isActive(world, x, y, z);
						return binIcons[tileEntity.tier.ordinal()][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					case 14:
						TileEntityThermalEvaporationController tileEntity1 = (TileEntityThermalEvaporationController)world.getTileEntity(x, y, z);

						if(side == tileEntity1.facing)
						{
							return MekanismUtils.isActive(world, x, y, z) ? icons[meta][1] : icons[meta][0];
						} 
						else {
							return icons[meta][2];
						}
					default:
						return getIcon(side, meta);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					case 2:
						TileEntityInductionPort tileEntity = (TileEntityInductionPort)world.getTileEntity(x, y, z);
						return icons[meta][tileEntity.mode ? 1 : 0];
					case 3:
						TileEntityInductionCell tileEntity1 = (TileEntityInductionCell)world.getTileEntity(x, y, z);
						return icons[meta][tileEntity1.tier.ordinal()];
					case 4:
						TileEntityInductionProvider tileEntity2 = (TileEntityInductionProvider)world.getTileEntity(x, y, z);
						return icons[meta][tileEntity2.tier.ordinal()];
					case 5:
						TileEntitySuperheatingElement element = (TileEntitySuperheatingElement)world.getTileEntity(x, y, z);
						
						if(element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null)
						{
							return icons[meta][SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 1 : 0];
						}
						
						return icons[meta][0];
					default:
						return getIcon(side, meta);
				}
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 14:
						if(side == 2)
						{
							return icons[meta][0];
						} 
						else {
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

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(BasicType type : BasicType.values())
		{
			if(type.typeBlock == blockType)
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
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);

		switch(blockType)
		{
			case BASIC_BLOCK_1:
				switch(meta)
				{
					case 10:
						return false;
					case 9:
					case 11:
						TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getTileEntity(x, y, z);

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
						return super.canCreatureSpawn(type, world, x, y, z);
				}
			case BASIC_BLOCK_2:
				switch(meta)
				{
					case 1:
					case 2:
					case 7:
					case 8:
						TileEntityMultiblock tileEntity = (TileEntityMultiblock)world.getTileEntity(x, y, z);

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
						return super.canCreatureSpawn(type, world, x, y, z);
				}
			default:
				return super.canCreatureSpawn(type, world, x, y, z);
		}
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		BasicType type = BasicType.get(this, world.getBlockMetadata(x, y, z));

		if(!world.isRemote && type == BasicType.BIN)
		{
			TileEntityBin bin = (TileEntityBin)world.getTileEntity(x, y, z);
			MovingObjectPosition pos = MekanismUtils.rayTrace(world, player);

			if(pos != null && pos.sideHit == bin.facing)
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		BasicType type = BasicType.get(this, metadata);
		TileEntity tile = world.getTileEntity(x, y, z);

		if(type == BasicType.REFINED_OBSIDIAN)
		{
			if(entityplayer.isSneaking())
			{
				entityplayer.openGui(Mekanism.instance, 1, world, x, y, z);
				return true;
			}
		}

		if(tile instanceof TileEntityThermalEvaporationController)
		{
			if(!entityplayer.isSneaking())
			{
				if(!world.isRemote)
				{
					entityplayer.openGui(Mekanism.instance, 33, world, x, y, z);
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
					if(owner == null || entityplayer.getCommandSenderName().equals(owner))
					{
						entityplayer.openGui(Mekanism.instance, 57, world, x, y, z);
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
			TileEntityBin bin = (TileEntityBin)world.getTileEntity(x, y, z);

			if(entityplayer.getCurrentEquippedItem() != null && MekanismUtils.hasUsableWrench(entityplayer, x, y, z))
			{
				if(!world.isRemote)
				{
					Item tool = entityplayer.getCurrentEquippedItem().getItem();
					
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, x, y, z, false);
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);
					}
	
					int change = ForgeDirection.ROTATION_MATRIX[ForgeDirection.UP.ordinal()][bin.facing];
	
					bin.setFacing((short)change);
					world.notifyBlocksOfNeighborChange(x, y, z, this);
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
	
							((EntityPlayerMP)entityplayer).sendContainerAndContentsToPlayer(entityplayer.openContainer, entityplayer.openContainer.getInventory());
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
			
			return ((IMultiblock)world.getTileEntity(x, y, z)).onActivate(entityplayer);
		}
		else if(tile instanceof IStructuralMultiblock)
		{
			if(world.isRemote)
			{
				return true;
			}
			
			return ((IStructuralMultiblock)world.getTileEntity(x, y, z)).onActivate(entityplayer);
		}

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return BasicType.get(this, world.getBlockMetadata(x, y, z)) != BasicType.STRUCTURAL_GLASS;
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
		return Mekanism.proxy.CTM_RENDER_ID;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		if(blockType == BasicBlock.BASIC_BLOCK_1)
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
		else if(blockType == BasicBlock.BASIC_BLOCK_2)
		{
			if(metadata == 5)
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
	public boolean hasTileEntity(int metadata)
	{
		BasicType type = BasicType.get(blockType, metadata);
		
		return type != null && type.tileEntityClass != null;
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if(!world.isRemote)
		{
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onAdded();
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if(BasicType.get(blockType, metadata) == null)
		{
			return null;
		}

		return BasicType.get(blockType, metadata).create();
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		if(world.getTileEntity(x, y, z) instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
			int side = MathHelper.floor_double((entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
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
			tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);
			
			if(tileEntity instanceof TileEntitySecurityDesk)
			{
				((TileEntitySecurityDesk)tileEntity).owner = entityliving.getCommandSenderName();
			}

			if(tileEntity instanceof IBoundingBlock)
			{
				((IBoundingBlock)tileEntity).onPlace();
			}
		}

		world.func_147479_m(x, y, z);
		world.updateLightByType(EnumSkyBlock.Block, x, y, z);
	    world.updateLightByType(EnumSkyBlock.Sky, x, y, z);

		if(!world.isRemote && world.getTileEntity(x, y, z) != null)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).update();
			}
			
			if(tileEntity instanceof IStructuralMultiblock)
			{
				((IStructuralMultiblock)tileEntity).update();
			}
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, x, y, z, block, meta);
		
		world.removeTileEntity(x, y, z);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		BasicType type = BasicType.get(this, world.getBlockMetadata(x, y, z));
		ItemStack ret = new ItemStack(this, 1, type.meta);

		if(type == BasicType.BIN)
		{
			TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);
			InventoryBin inv = new InventoryBin(ret);

			((ITierItem)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
			inv.setItemCount(tileEntity.getItemCount());
			
			if(tileEntity.getItemCount() > 0)
			{
				inv.setItemType(tileEntity.itemType);
			}
		}
		else if(type == BasicType.INDUCTION_CELL)
		{
			TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(x, y, z);
			((ItemBlockBasic)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
		}
		else if(type == BasicType.INDUCTION_PROVIDER)
		{
			TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(x, y, z);
			((ItemBlockBasic)ret.getItem()).setBaseTier(ret, tileEntity.tier.getBaseTier());
		}
		
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if(tileEntity instanceof IStrictEnergyStorage)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)ret.getItem();
			energizedItem.setEnergy(ret, ((IStrictEnergyStorage)tileEntity).getEnergy());
		}

		return ret;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(x, y, z);
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z, null);

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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		Coord4D obj = new Coord4D(x, y, z).getFromSide(ForgeDirection.getOrientation(side).getOpposite());
		
		if(BasicType.get(this, obj.getMetadata(world)) == BasicType.STRUCTURAL_GLASS)
		{
			return ctms[10][0].shouldRenderSide(world, x, y, z, side);
		}
		else {
			return super.shouldSideBeRendered(world, x, y, z, side);
		}
	}

	@Override
	public ForgeDirection[] getValidRotations(World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		ForgeDirection[] valid = new ForgeDirection[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
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
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
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
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(world, x, y, z))
		{
			return ctms[meta][1];
		}
		
		BasicType type = BasicType.get(this, world.getBlockMetadata(x, y, z));

		if(type == BasicType.INDUCTION_CELL)
		{
			TileEntityInductionCell tileEntity = (TileEntityInductionCell)world.getTileEntity(x, y, z);
			return ctms[meta][tileEntity.tier.ordinal()];
		}
		else if(type == BasicType.INDUCTION_PROVIDER)
		{
			TileEntityInductionProvider tileEntity = (TileEntityInductionProvider)world.getTileEntity(x, y, z);
			return ctms[meta][tileEntity.tier.ordinal()];
		}
		else if(type == BasicType.SUPERHEATING_ELEMENT)
		{
			TileEntitySuperheatingElement element = (TileEntitySuperheatingElement)world.getTileEntity(x, y, z);
			
			if(element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null)
			{
				return ctms[meta][SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 1 : 0];
			}
			
			return ctms[meta][0];
		}

		return ctms[meta][0];
	}
	
	@Override
	public boolean shouldRenderBlock(IBlockAccess world, int x, int y, int z, int meta)
	{
		return BasicType.get(this, world.getBlockMetadata(x, y, z)) != BasicType.SECURITY_DESK;
	}
	
	public static enum BasicType
	{
		OSMIUM_BLOCK(BasicBlock.BASIC_BLOCK_1, 0, "OsmiumBlock", null, false),
		BRONZE_BLOCK(BasicBlock.BASIC_BLOCK_1, 1, "BronzeBlock", null, false),
		REFINED_OBSIDIAN(BasicBlock.BASIC_BLOCK_1, 2, "RefinedObsidian", null, false),
		CHARCOAL_BLOCK(BasicBlock.BASIC_BLOCK_1, 3, "CharcoalBlock", null, false),
		REFINED_GLOWSTONE(BasicBlock.BASIC_BLOCK_1, 4, "RefinedGlowstone", null, false),
		STEEL_BLOCK(BasicBlock.BASIC_BLOCK_1, 5, "SteelBlock", null, false),
		BIN(BasicBlock.BASIC_BLOCK_1, 6, "Bin", TileEntityBin.class, true),
		TELEPORTER_FRAME(BasicBlock.BASIC_BLOCK_1, 7, "TeleporterFrame", null, true),
		STEEL_CASING(BasicBlock.BASIC_BLOCK_1, 8, "SteelCasing", null, true),
		DYNAMIC_TANK(BasicBlock.BASIC_BLOCK_1, 9, "DynamicTank", TileEntityDynamicTank.class, true),
		STRUCTURAL_GLASS(BasicBlock.BASIC_BLOCK_1, 10, "StructuralGlass", TileEntityStructuralGlass.class, true),
		DYNAMIC_VALVE(BasicBlock.BASIC_BLOCK_1, 11, "DynamicValve", TileEntityDynamicValve.class, true),
		COPPER_BLOCK(BasicBlock.BASIC_BLOCK_1, 12, "CopperBlock", null, false),
		TIN_BLOCK(BasicBlock.BASIC_BLOCK_1, 13, "TinBlock", null, false),
		THERMAL_EVAPORATION_CONTROLLER(BasicBlock.BASIC_BLOCK_1, 14, "ThermalEvaporationController", TileEntityThermalEvaporationController.class, true),
		THERMAL_EVAPORATION_VALVE(BasicBlock.BASIC_BLOCK_1, 15, "ThermalEvaporationValve", TileEntityThermalEvaporationValve.class, true),
		THERMAL_EVAPORATION_BLOCK(BasicBlock.BASIC_BLOCK_2, 0, "ThermalEvaporationBlock", TileEntityThermalEvaporationBlock.class, true),
		INDUCTION_CASING(BasicBlock.BASIC_BLOCK_2, 1, "InductionCasing", TileEntityInductionCasing.class, true),
		INDUCTION_PORT(BasicBlock.BASIC_BLOCK_2, 2, "InductionPort", TileEntityInductionPort.class, true),
		INDUCTION_CELL(BasicBlock.BASIC_BLOCK_2, 3, "InductionCell", TileEntityInductionCell.class, true),
		INDUCTION_PROVIDER(BasicBlock.BASIC_BLOCK_2, 4, "InductionProvider", TileEntityInductionProvider.class, true),
		SUPERHEATING_ELEMENT(BasicBlock.BASIC_BLOCK_2, 5, "SuperheatingElement", TileEntitySuperheatingElement.class, true),
		PRESSURE_DISPERSER(BasicBlock.BASIC_BLOCK_2, 6, "PressureDisperser", TileEntityPressureDisperser.class, true),
		BOILER_CASING(BasicBlock.BASIC_BLOCK_2, 7, "BoilerCasing", TileEntityBoilerCasing.class, true),
		BOILER_VALVE(BasicBlock.BASIC_BLOCK_2, 8, "BoilerValve", TileEntityBoilerValve.class, true),
		SECURITY_DESK(BasicBlock.BASIC_BLOCK_2, 9, "SecurityDesk", TileEntitySecurityDesk.class, true);
		
		public BasicBlock typeBlock;
		public int meta;
		public String name;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasDescription;

		private BasicType(BasicBlock block, int i, String s, Class<? extends TileEntity> tileClass, boolean hasDesc)
		{
			typeBlock = block;
			meta = i;
			name = s;
			tileEntityClass = tileClass;
			hasDescription = hasDesc;
		}

		public static BasicType get(Block block, int meta)
		{
			if(block instanceof BlockBasic)
			{
				return get(((BlockBasic)block).blockType, meta);
			}

			return null;
		}

		public static BasicType get(BasicBlock block, int meta)
		{
			for(BasicType type : values())
			{
				if(type.meta == meta && type.typeBlock == block)
				{
					return type;
				}
			}

			return null;
		}

		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}

		public String getDescription()
		{
			return LangUtils.localize("tooltip." + name);
		}

		public ItemStack getStack()
		{
			return new ItemStack(typeBlock.getBlock(), 1, meta);
		}

		public static BasicType get(ItemStack stack)
		{
			return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
		}
	}

	public static enum BasicBlock
	{
		BASIC_BLOCK_1,
		BASIC_BLOCK_2;
		
		public Block getBlock()
		{
			switch(this)
			{
				case BASIC_BLOCK_1:
					return MekanismBlocks.BasicBlock;
				case BASIC_BLOCK_2:
					return MekanismBlocks.BasicBlock2;
				default:
					return null;
			}
		}
	}
}