package mekanism.common.block;

import java.util.Random;

import mekanism.api.IMekWrench;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateEnergyCube;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;

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
public class BlockEnergyCube extends BlockContainer
{
	public BlockEnergyCube()
	{
		super(Material.IRON);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public BlockStateContainer createBlockState()
	{
		return new BlockStateEnergyCube(this);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState();
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		TileEntity tile = MekanismUtils.getTileEntitySafe(worldIn, pos);
		
		if(tile instanceof TileEntityEnergyCube)
		{
			TileEntityEnergyCube cube = (TileEntityEnergyCube)tile;
			
			if(cube.facing != null)
			{
				state = state.withProperty(BlockStateFacing.facingProperty, cube.facing);
			}

			if(cube.tier != null)
			{
				state = state.withProperty(BlockStateEnergyCube.typeProperty, cube.tier);
			}
		}

		return state;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(neighborBlock);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(pos);
		int side = MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(placer.rotationPitch);
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
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(pos) > 0;
	}

	@Override
	public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list)
	{
		for(EnergyCubeTier tier : EnergyCubeTier.values())
		{
			ItemStack discharged = new ItemStack(this);
			((ItemBlockEnergyCube)discharged.getItem()).setBaseTier(discharged, tier.getBaseTier());
			list.add(discharged);
			ItemStack charged = new ItemStack(this);
			((ItemBlockEnergyCube)charged.getItem()).setBaseTier(charged, tier.getBaseTier());
			((ItemBlockEnergyCube)charged.getItem()).setEnergy(charged, tier.maxEnergy);
			list.add(charged);
		}
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);
		ItemStack stack = entityplayer.getHeldItem(hand);

		if(!stack.isEmpty())
		{
			IMekWrench wrenchHandler = Wrenches.getHandler(stack);
			if (wrenchHandler != null) {
				RayTraceResult raytrace = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
				if(wrenchHandler.canUseWrench(entityplayer, hand, stack, raytrace)) {
					if(SecurityUtils.canAccess(entityplayer, tileEntity)) {

						wrenchHandler.wrenchUsed(entityplayer, hand, stack, raytrace);

						if(entityplayer.isSneaking())
						{
							MekanismUtils.dismantleBlock(this, state, world, pos);
							return true;
						}

						if(tileEntity != null)
						{
							int change = tileEntity.facing.rotateAround(side.getAxis()).ordinal();

							tileEntity.setFacing((short)change);
							world.notifyNeighborsOfStateChange(pos, this, true);
						}
					} else {
						SecurityUtils.displayNoAccess(entityplayer);
					}

					return true;
				}
			}
		}

		if(tileEntity != null)
		{
			if(!entityplayer.isSneaking())
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					entityplayer.openGui(Mekanism.instance, 8, world, pos.getX(), pos.getY(), pos.getZ());
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
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityEnergyCube();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	private ItemStack getDropItem(IBlockAccess world, BlockPos pos) {
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);
		ItemStack itemStack = new ItemStack(MekanismBlocks.EnergyCube);
		
		if(!itemStack.hasTagCompound())
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		if(tileEntity instanceof ISecurityTile)
		{
			ISecurityItem securityItem = (ISecurityItem)itemStack.getItem();
			
			if(securityItem.hasSecurity(itemStack))
			{
				securityItem.setOwnerUUID(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwnerUUID());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}
		
		if(tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;

			config.getConfig().write(ItemDataUtils.getDataMap(itemStack));
			config.getEjector().write(ItemDataUtils.getDataMap(itemStack));
		}

		ITierItem tierItem = (ITierItem)itemStack.getItem();
		tierItem.setBaseTier(itemStack, tileEntity.tier.getBaseTier());

		IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
		energizedItem.setEnergy(itemStack, tileEntity.electricityStored);

		ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
		inventory.setInventory(tileEntity.getInventory(), itemStack);

		return itemStack;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return getDropItem(world, pos);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		drops.add(getDropItem(world, pos));
	}

	/**
	 * {@inheritDoc}
	 * Keep tile entity in world until after
	 * {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)}.
	 * Used together with {@link Block#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)}.
	 *
	 * @author Forge
	 * @see BlockFlowerPot#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)
	 */
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
	                               boolean willHarvest)
	{
		return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	/**
	 * {@inheritDoc}
	 * Used together with {@link Block#removedByPlayer(IBlockState, World, BlockPos, EntityPlayer, boolean)}.
	 *
	 * @author Forge
	 * @see BlockFlowerPot#harvestBlock(World, EntityPlayer, BlockPos, IBlockState, TileEntity, ItemStack)
	 */
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos,
	                         IBlockState state, TileEntity te, ItemStack stack)
	{
		MekanismUtils.harvestBlockPatched(this, getDropItem(world, pos), world, player, pos, te);
		world.setBlockToAir(pos);
	}

	/**
	 * Returns that this "cannot" be silk touched.
	 * This is so that {@link Block#getSilkTouchDrop(IBlockState)} is not called, because only
	 * {@link Block#getDrops(NonNullList, IBlockAccess, BlockPos, IBlockState, int)} supports tile entities.
	 * Our blocks keep their inventory and other behave like they are being silk touched by default anyway.
	 *
	 * @return false
	 */
	@Override
	protected boolean canSilkHarvest()
	{
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);
		return tileEntity.getRedstoneLevel();
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return true;
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
}
