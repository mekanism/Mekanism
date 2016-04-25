package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.energy.IEnergizedItem;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.IEnergyCube;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.states.BlockStateEnergyCube;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.api.tools.IToolWrench;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		super(Material.iron);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public BlockState createBlockState()
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
		TileEntity tile = worldIn.getTileEntity(pos);
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

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) 
	{
		blockIcon = register.registerIcon(BlockBasic.ICON_BASE);
	}
*/

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
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
		int side = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
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
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
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
		}
	}
	
	@Override
	public float getBlockHardness(World world, BlockPos pos)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof ISecurityTile)
		{
			return SecurityUtils.getSecurity((ISecurityTile)tile) == SecurityMode.PUBLIC ? blockHardness : -1;
		}
		
		return blockHardness;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, pos))
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, pos, false);
						
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
	                {
	                    ((IToolWrench) tool).wrenchUsed(entityplayer, pos);
	                }
	
					int change = tileEntity.facing.rotateAround(side.getAxis()).ordinal();
	
					tileEntity.setFacing((short)change);
					world.notifyNeighborsOfStateChange(pos, this);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
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

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityEnergyCube();
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
    public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
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
				securityItem.setOwner(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwner());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}

		IEnergyCube energyCube = (IEnergyCube)itemStack.getItem();
		energyCube.setEnergyCubeTier(itemStack, tileEntity.tier);

		IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
		energizedItem.setEnergy(itemStack, tileEntity.electricityStored);

		ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
		inventory.setInventory(tileEntity.getInventory(), itemStack);

		return itemStack;
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
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, BlockPos pos)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);
		return tileEntity.getRedstoneLevel();
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
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
