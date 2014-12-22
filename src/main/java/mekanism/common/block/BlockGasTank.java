package mekanism.common.block;

import java.util.Random;

import mekanism.api.gas.IGasItem;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ModAPIManager;

import buildcraft.api.tools.IToolWrench;

public class BlockGasTank extends BlockContainer
{
	public BlockGasTank()
	{
		super(Material.iron);
		setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 1.0F, 0.8F);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap register) {}
*/

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);

		worldIn.setBlockState(pos, state.withProperty(BlockStateFacing.facingProperty, placer.getHorizontalFacing().getOpposite()), 2);
		tileEntity.redstone = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
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

		TileEntityGasTank tileEntity = (TileEntityGasTank)worldIn.getTileEntity(pos);

		if(playerIn.getCurrentEquippedItem() != null)
		{
			Item tool = playerIn.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(playerIn, pos))
			{
				if(playerIn.isSneaking())
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
			if(!playerIn.isSneaking())
			{
				playerIn.openGui(Mekanism.instance, 10, worldIn, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		return false;
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
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune)
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
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityGasTank();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		TileEntityGasTank tileEntity = (TileEntityGasTank)world.getTileEntity(pos);
		ItemStack itemStack = new ItemStack(MekanismBlocks.GasTank);

		IGasItem storageTank = (IGasItem)itemStack.getItem();
		storageTank.setGas(itemStack, tileEntity.gasTank.getGas());

		ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
		inventory.setInventory(tileEntity.getInventory(), itemStack);

		return itemStack;
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override

	public int getComparatorInputOverride(World worldIn, BlockPos pos)
	{
		TileEntityGasTank tileEntity = (TileEntityGasTank)worldIn.getTileEntity(pos);
		return tileEntity.getRedstoneLevel();
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
