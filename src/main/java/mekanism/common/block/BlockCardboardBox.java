package mekanism.common.block;

import static mekanism.common.block.states.BlockStateCardboardBox.storageProperty;

import java.util.Random;

import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.states.BlockStateCardboardBox;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockCardboardBox extends BlockContainer
{
	private static boolean testingPlace = false;
	
	public BlockCardboardBox()
	{
		super(Material.CLOTH);
		setCreativeTab(Mekanism.tabMekanism);
		setHardness(0.5F);
		setResistance(1F);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateCardboardBox(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(storageProperty, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(storageProperty) ? 1 : 0;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos)
	{
		return testingPlace;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote && entityplayer.isSneaking())
		{
			TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getTileEntity(pos);

			if(tileEntity.storedData != null)
			{
				BlockData data = tileEntity.storedData;
				
				testingPlace = true;
				
				if(!data.block.canPlaceBlockAt(world, pos))
				{
					testingPlace = false;
					return true;
				}
				
				testingPlace = false;

				if(data.block != null)
				{
					IBlockState newstate = data.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, data.meta, entityplayer, hand);
					data.meta = newstate.getBlock().getMetaFromState(newstate);
				}

				world.setBlockState(pos, data.block.getStateFromMeta(data.meta), 3);

				if(data.tileTag != null && world.getTileEntity(pos) != null)
				{
					data.updateLocation(pos);
					world.getTileEntity(pos).readFromNBT(data.tileTag);
				}

				if(data.block != null)
				{
					data.block.onBlockPlacedBy(world, pos, data.block.getStateFromMeta(data.meta), entityplayer, new ItemStack(data.block, 1, data.meta));
				}

				spawnAsEntity(world, pos, new ItemStack(MekanismBlocks.CardboardBox));
			}
		}

		return entityplayer.isSneaking();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityCardboardBox();
	}

	private ItemStack getDropItem(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getTileEntity(pos);

		Item item = Item.getItemFromBlock(state.getBlock());
		ItemStack itemStack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));

		if(tileEntity.storedData != null)
		{
			((ItemBlockCardboardBox)item).setBlockData(itemStack, tileEntity.storedData);
		}

		return itemStack;
	}

	@Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return getDropItem(state, world, pos);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		drops.add(getDropItem(state, world, pos));
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
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack)
	{
		MekanismUtils.harvestBlockPatched(this, getDropItem(state, world, pos), world, player, pos, te);
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

	public static class BlockData
	{
		public Block block;
		public int meta;
		public NBTTagCompound tileTag;

		public BlockData(Block b, int j, NBTTagCompound nbtTags)
		{
			block = b;
			meta = j;
			tileTag = nbtTags;
		}

		public BlockData() {}

		public void updateLocation(BlockPos pos)
		{
			if(tileTag != null)
			{
				tileTag.setInteger("x", pos.getX());
				tileTag.setInteger("y", pos.getY());
				tileTag.setInteger("z", pos.getZ());
			}
		}

		public NBTTagCompound write(NBTTagCompound nbtTags)
		{
			nbtTags.setInteger("id", Block.getIdFromBlock(block));
			nbtTags.setInteger("meta", meta);

			if(tileTag != null)
			{
				nbtTags.setTag("tileTag", tileTag);
			}

			return nbtTags;
		}

		public static BlockData read(NBTTagCompound nbtTags)
		{
			BlockData data = new BlockData();

			data.block = Block.getBlockById(nbtTags.getInteger("id"));
			data.meta = nbtTags.getInteger("meta");

			if(nbtTags.hasKey("tileTag"))
			{
				data.tileTag = nbtTags.getCompoundTag("tileTag");
			}

			return data;
		}
	}

	/**
	 * If the player is sneaking and the dest block is a cardboard box, ensure onBlockActivated is called, and that the item use is not.
	 * @param blockEvent event
	 */
	@SubscribeEvent
	public void rightClickEvent(PlayerInteractEvent.RightClickBlock blockEvent){
		if (blockEvent.getEntityPlayer().isSneaking() && blockEvent.getWorld().getBlockState(blockEvent.getPos()).getBlock() == this){
			blockEvent.setUseBlock(Event.Result.ALLOW);
			blockEvent.setUseItem(Event.Result.DENY);
		}
	}
}
