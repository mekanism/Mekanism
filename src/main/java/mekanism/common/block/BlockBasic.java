package mekanism.common.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.client.ClientProxy;
import mekanism.common.ConnectedTextureRenderer;
import mekanism.common.IActiveState;
import mekanism.common.IBoundingBlock;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tank.TankUpdateProtocol;
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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
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
public class BlockBasic extends Block
{
	public IIcon[][] icons = new IIcon[256][6];

	public ConnectedTextureRenderer glassRenderer = new ConnectedTextureRenderer("glass/DynamicGlass", this, Arrays.asList(10));

	public BlockBasic()
	{
		super(Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(block == this && tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		if(this == MekanismBlocks.BasicBlock)
		{
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
			icons[9][0] = register.registerIcon("mekanism:DynamicTank");
			icons[10][0] = register.registerIcon("mekanism:DynamicGlass");
			icons[11][0] = register.registerIcon("mekanism:DynamicValve");
			icons[12][0] = register.registerIcon("mekanism:CopperBlock");
			icons[13][0] = register.registerIcon("mekanism:TinBlock");
			icons[14][0] = register.registerIcon("mekanism:SalinationController");
			icons[14][1] = register.registerIcon("mekanism:SalinationControllerOn");
			icons[14][2] = register.registerIcon("mekanism:SalinationBlock");
			icons[15][0] = register.registerIcon("mekanism:SalinationValve");

			glassRenderer.registerIcons(register);
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			icons[0][0] = register.registerIcon("mekanism:SalinationBlock");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(this == MekanismBlocks.BasicBlock)
		{
			if(metadata == 6)
			{
				TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

				if(side == 0 || side == 1)
				{
					return MekanismUtils.isActive(world, x, y, z) ? icons[6][3] : icons[6][1];
				}
				else if(side == tileEntity.facing)
				{
					return MekanismUtils.isActive(world, x, y, z) ? icons[6][4] : icons[6][2];
				}
				else {
					return icons[6][0];
				}
			}
			else if(metadata == 10)
			{
				return glassRenderer.getIcon(world, x, y, z, side);
			}
			else if(metadata == 14)
			{
				TileEntitySalinationController tileEntity = (TileEntitySalinationController)world.getTileEntity(x, y, z);

				if(side == tileEntity.facing)
				{
					return tileEntity.structured ? icons[14][1] : icons[14][0];
				}
				else {
					return icons[14][2];
				}
			}
			else {
				return getIcon(side, metadata);
			}
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			return getIcon(side, metadata);
		}

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if(this == MekanismBlocks.BasicBlock)
		{
			if(meta != 6 && meta != 14)
			{
				return icons[meta][0];
			}
			else if(meta == 6)
			{
				if(side == 0 || side == 1)
				{
					return icons[6][1];
				}
				else if(side == 3)
				{
					return icons[6][2];
				}
				else {
					return icons[6][0];
				}
			}
			else if(meta == 14)
			{
				if(side == 3)
				{
					return icons[14][0];
				}
				else {
					return icons[14][2];
				}
			}
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			return icons[meta][0];
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
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		if(this == MekanismBlocks.BasicBlock)
		{
			list.add(new ItemStack(item, 1, 0));
			list.add(new ItemStack(item, 1, 1));
			list.add(new ItemStack(item, 1, 2));
			list.add(new ItemStack(item, 1, 3));
			list.add(new ItemStack(item, 1, 4));
			list.add(new ItemStack(item, 1, 5));
			list.add(new ItemStack(item, 1, 6));
			list.add(new ItemStack(item, 1, 7));
			list.add(new ItemStack(item, 1, 8));
			list.add(new ItemStack(item, 1, 9));
			list.add(new ItemStack(item, 1, 10));
			list.add(new ItemStack(item, 1, 11));
			list.add(new ItemStack(item, 1, 12));
			list.add(new ItemStack(item, 1, 13));
			list.add(new ItemStack(item, 1, 14));
			list.add(new ItemStack(item, 1, 15));
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			list.add(new ItemStack(item, 1, 0));
		}
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);

		if(this == MekanismBlocks.BasicBlock)
		{
			if(meta == 9 || meta == 10 || meta == 11)
			{
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
			}
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{

		}

		return super.canCreatureSpawn(type, world, x, y, z);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		int meta = world.getBlockMetadata(x, y, z);

		if(this == MekanismBlocks.BasicBlock)
		{
			if(!world.isRemote && meta == 6)
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
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(this == MekanismBlocks.BasicBlock)
		{
			if(metadata != 6)
			{
				if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
				{
					return false;
				}
			}

			if(metadata == 2)
			{
				if(entityplayer.isSneaking())
				{
					entityplayer.openGui(Mekanism.instance, 1, world, x, y, z);
					return true;
				}
			}

			if(metadata == 14)
			{
				if(!entityplayer.isSneaking())
				{
					entityplayer.openGui(Mekanism.instance, 33, world, x, y, z);
					return true;
				}
			}

			if(world.isRemote)
			{
				return true;
			}

			if(metadata == 6)
			{
				TileEntityBin bin = (TileEntityBin)world.getTileEntity(x, y, z);

				if(bin.getItemCount() < bin.MAX_STORAGE)
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

							((EntityPlayerMP)entityplayer).sendContainerAndContentsToPlayer(entityplayer.openContainer, entityplayer.openContainer.getInventory());
						}
					}
				}

				return true;
			}
			else if(metadata == 9 || metadata == 10 || metadata == 11)
			{
				if(!entityplayer.isSneaking() && ((TileEntityDynamicTank)world.getTileEntity(x, y, z)).structure != null)
				{
					TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getTileEntity(x, y, z);

					if(!manageInventory(entityplayer, tileEntity))
					{
						Mekanism.packetHandler.sendToAll(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
						entityplayer.openGui(Mekanism.instance, 18, world, x, y, z);
					}
					else {
						entityplayer.inventory.markDirty();
						tileEntity.sendPacketToRenderer();
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return !(this == MekanismBlocks.BasicBlock && world.getBlockMetadata(x, y, z) == 10);
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
		return ClientProxy.BASIC_RENDER_ID;
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

		if(this == MekanismBlocks.BasicBlock)
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

		return 0;
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		if(this == MekanismBlocks.BasicBlock)
		{
			return metadata == 6 || metadata == 9 || metadata == 10 || metadata == 11 || metadata == 12 || metadata == 14 || metadata == 15;
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			return metadata == 0;
		}

		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if(this == MekanismBlocks.BasicBlock)
		{
			switch(metadata)
			{
				case 6:
					return new TileEntityBin();
				case 9:
					return new TileEntityDynamicTank();
				case 10:
					return new TileEntityDynamicTank();
				case 11:
					return new TileEntityDynamicValve();
				case 14:
					return new TileEntitySalinationController();
				case 15:
					return new TileEntitySalinationValve();
			}
		}
		else if(this == MekanismBlocks.BasicBlock2)
		{
			switch(metadata)
			{
				case 0:
					return new TileEntitySalinationBlock();
			}
		}

		return null;
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

			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		ItemStack ret = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));

		if(this == MekanismBlocks.BasicBlock)
		{
			if(ret.getItemDamage() == 6)
			{
				TileEntityBin tileEntity = (TileEntityBin)world.getTileEntity(x, y, z);
				InventoryBin inv = new InventoryBin(ret);

				inv.setItemCount(tileEntity.getItemCount());

				if(tileEntity.getItemCount() > 0)
				{
					inv.setItemType(tileEntity.itemType);
				}
			}
		}

		return ret;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(x, y, z);
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

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		if(this == MekanismBlocks.BasicBlock && world.getBlockMetadata(x, y, z) == 10)
		{
			return glassRenderer.shouldRenderSide(world, x, y, z, side);
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
}