package mekanism.common.block;

import java.util.Random;

import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
public class BlockBounding extends Block implements IPeripheralProvider
{
	public BlockBounding()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing facing, float playerX, float playerY, float playerZ)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			return world.getBlockState(tileEntity.mainPos).getBlock().onBlockActivated(world, tileEntity.mainPos, world.getBlockState(tileEntity.mainPos), entityplayer, facing, playerX, playerY, playerZ);
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			return world.getBlockState(tileEntity.mainPos).getBlock().getPickBlock(target, world, tileEntity.mainPos);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			return world.getBlockState(tileEntity.mainPos).getBlock().removedByPlayer(world, tileEntity.mainPos, player, willHarvest);
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			tileEntity.onNeighborChange(world.getBlockState(neighbor).getBlock());
			world.getBlockState(tileEntity.mainPos).getBlock().onNeighborChange(world, tileEntity.mainPos, pos);
		} catch(Exception e) {}
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return null;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

/*
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
*/

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if(!(Boolean)state.getValue(BlockStateBounding.advancedProperty))
		{
			return new TileEntityBoundingBlock();
		}
		else
		{
			return new TileEntityAdvancedBoundingBlock();
		}
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
	protected BlockState createBlockState()
	{
		return new BlockStateBounding(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(BlockStateBounding.advancedProperty, meta > 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((Boolean)state.getValue(BlockStateBounding.advancedProperty)) ? 1 : 0;
	}
}
