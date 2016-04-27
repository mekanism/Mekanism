package mekanism.common.block;

import java.util.Random;

import mekanism.common.block.states.BlockStateBounding;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockBounding extends Block
{
	public BlockBounding()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
	}

	public BlockState createBlockState()
	{
		return new BlockStateBounding(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		boolean isAdvanced = meta > 0;

		return this.getDefaultState().withProperty(BlockStateBounding.advancedProperty, isAdvanced);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		boolean isAdvanced = state.getValue(BlockStateBounding.advancedProperty);
		return isAdvanced ? 1 : 0;
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) 
	{
		blockIcon = register.registerIcon(BlockBasic.ICON_BASE);
	}
*/

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			IBlockState state1 = world.getBlockState(tileEntity.mainPos);
			return state1.getBlock().onBlockActivated(world, tileEntity.mainPos, state1, entityplayer, side, hitX, hitY, hitZ);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		super.breakBlock(world, pos, state);
		
		world.removeTileEntity(pos);
	}

	@Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			IBlockState state1 = world.getBlockState(tileEntity.mainPos);
			return state1.getBlock().getPickBlock(target, world, tileEntity.mainPos, player);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			IBlockState state1 = world.getBlockState(tileEntity.mainPos);
			return state1.getBlock().removedByPlayer(world, tileEntity.mainPos, player, willHarvest);
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			tileEntity.onNeighborChange(state.getBlock());
			IBlockState state1 = world.getBlockState(tileEntity.mainPos);
			state1.getBlock().onNeighborBlockChange(world, tileEntity.mainPos, state1, this);
		} catch(Exception e) {}
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getTileEntity(pos);
			return world.getBlockState(tileEntity.mainPos).getBlock().getPlayerRelativeBlockHardness(player, world, tileEntity.mainPos);
		} catch(Exception e) {
			return super.getPlayerRelativeBlockHardness(player, world, pos);
		}
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

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if(state.getValue(BlockStateBounding.advancedProperty))
		{
			return new TileEntityAdvancedBoundingBlock();
		}
		else
		{
			return new TileEntityBoundingBlock();
		}
	}
}
