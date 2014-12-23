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
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateEnergyCube;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Block class for handling multiple energy cube block IDs.
 * 0: Basic Energy Cube
 * 1: Advanced Energy Cube
 * 2: Elite Energy Cube
 * @author AidanBrady
 *
 */
@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
public class BlockEnergyCube extends BlockContainer implements IPeripheralProvider
{
	public TextureAtlasSprite[][] icons = new TextureAtlasSprite[256][256];

	public BlockEnergyCube()
	{
		super(Material.iron);
		setHardness(2F);
		setResistance(4F);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap register) {}
*/

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
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)worldIn.getTileEntity(pos);
		int height = Math.round(placer.rotationPitch);

		if(height >= 65)
		{
			worldIn.setBlockState(pos, state.withProperty(BlockStateFacing.facingProperty, EnumFacing.DOWN));
		}
		else if(height <= -65)
		{
			worldIn.setBlockState(pos, state.withProperty(BlockStateFacing.facingProperty, EnumFacing.UP));
		}
		else
		{
			worldIn.setBlockState(pos, state.withProperty(BlockStateFacing.facingProperty, placer.getHorizontalFacing().getOpposite()));
		}
		tileEntity.redstone = worldIn.isBlockIndirectlyGettingPowered(pos) > 0;
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
			((ItemBlockEnergyCube)charged.getItem()).setEnergy(charged, tier.MAX_ELECTRICITY);
			list.add(charged);
		};
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

		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)worldIn.getTileEntity(pos);

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
				playerIn.openGui(Mekanism.instance, 8, worldIn, pos.getX(), pos.getY(), pos.getZ());
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

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		TileEntityEnergyCube tile = new TileEntityEnergyCube();
		return tile;
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
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)world.getTileEntity(pos);
		ItemStack itemStack = new ItemStack(MekanismBlocks.EnergyCube);

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
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(World worldIn, BlockPos pos)
	{
		TileEntityEnergyCube tileEntity = (TileEntityEnergyCube)worldIn.getTileEntity(pos);
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
		return world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockStateFacing.facingProperty, axis));
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
		return new BlockStateEnergyCube(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		EnumFacing facing = EnumFacing.getFront((meta&0b111));

		EnergyCubeTier type = EnergyCubeTier.values()[meta >> 2];

		return this.getDefaultState().withProperty(BlockStateFacing.facingProperty, facing).withProperty(BlockStateEnergyCube.typeProperty, type);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		EnumFacing facing = (EnumFacing)state.getValue(BlockStateFacing.facingProperty);

		EnergyCubeTier type = (EnergyCubeTier)state.getValue(BlockStateEnergyCube.typeProperty);

		return type.ordinal() << 2 | facing.getIndex();
	}
}
