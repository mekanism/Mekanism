package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IEnergyCube;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Block class for handling multiple energy cube block IDs.
 * 0: Basic Energy Cube
 * 1: Advanced Energy Cube
 * 2: Elite Energy Cube
 * 3: Ultimate Energy Cube
 * 4: Creative Energy Cube
 * @author AidanBrady
 *
 */
@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
public class BlockEnergyCube extends BlockContainer implements IPeripheralProvider
{
	public BlockEnergyCube()
	{
		super(Material.iron);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) 
	{
		blockIcon = register.registerIcon(BlockBasic.ICON_BASE);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
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
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			ItemStack discharged = new ItemStack(this);
			discharged.setItemDamage(100);
			((ItemBlockEnergyCube)discharged.getItem()).setEnergyCubeTier(discharged, tier);
			list.add(discharged);
			ItemStack charged = new ItemStack(this);
			((ItemBlockEnergyCube)charged.getItem()).setEnergyCubeTier(charged, tier);
			((ItemBlockEnergyCube)charged.getItem()).setEnergy(charged, tier.maxEnergy);
			list.add(charged);
		};
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float f1, float f2, float f3)
	{
		if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
		{
			return false;
		}

		if(world.isRemote)
		{
			return true;
		}

		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(x, y, z);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, x, y, z))
			{
				if(entityplayer.isSneaking())
				{
					dismantleBlock(world, x, y, z, false);
					return true;
				}

				if(MekanismUtils.isBCWrench(tool))
					((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);

				int change = ForgeDirection.ROTATION_MATRIX[side][tileEntity.facing];

				tileEntity.setFacing((short)change);
				world.notifyBlocksOfNeighborChange(x, y, z, this);
				return true;
			}
		}

		if(tileEntity != null)
		{
			if(!entityplayer.isSneaking())
			{
				entityplayer.openGui(Mekanism.instance, 8, world, x, y, z);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
		{
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
	public TileEntity createNewTileEntity(World world, int meta)
	{
		TileEntityEnergyCube tile = new TileEntityEnergyCube();
		return tile;
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
		return -1;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(x, y, z);
		ItemStack itemStack = new ItemStack(MekanismBlocks.EnergyCube);

		IEnergyCube energyCube = (IEnergyCube)itemStack.getItem();
		energyCube.setEnergyCubeTier(itemStack, tileEntity.tier);

		IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
		energizedItem.setEnergy(itemStack, tileEntity.electricityStored);

		ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
		inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);

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

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int par5)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(x, y, z);
		return tileEntity.getRedstoneLevel();
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return true;
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
	@Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(x, y, z);

		if(te != null && te instanceof IPeripheral)
		{
			return (IPeripheral)te;
		}

		return null;
	}
}
