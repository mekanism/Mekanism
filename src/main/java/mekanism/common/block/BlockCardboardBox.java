package mekanism.common.block;

import java.util.Random;

import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.tile.TileEntityCardboardBox;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCardboardBox extends BlockContainer
{
	private static boolean testingPlace = false;
	
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[6];

	public BlockCardboardBox()
	{
		super(Material.cloth);
		setCreativeTab(Mekanism.tabMekanism);
		setHardness(0.5F);
		setResistance(1F);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister register)
	{
		icons[0] = register.registerIcon("mekanism:CardboardBoxTop");
		icons[1] = register.registerIcon("mekanism:CardboardBoxSide");
		icons[2] = register.registerIcon("mekanism:CardboardBoxSideStorage");
	}
*/

/*
	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int side, int meta)
	{
		if(side == 0 || side == 1)
		{
			return icons[0];
		}
		else {
			return meta == 0 ? icons[1] : icons[2];
		}
	}
*/

	@Override
	public boolean isReplaceable(World worldIn, BlockPos pos)
	{
		return testingPlace;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!worldIn.isRemote && playerIn.isSneaking())
		{
			ItemStack itemStack = new ItemStack(MekanismBlocks.CardboardBox);
			TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)worldIn.getTileEntity(pos);

			if(tileEntity.storedData != null)
			{
				BlockData data = tileEntity.storedData;
				
				testingPlace = true;
				
				if(!data.block.canPlaceBlockAt(worldIn, pos))
				{
					testingPlace = false;
					return true;
				}
				
				testingPlace = false;

				if(data.block != null)
				{
					IBlockState newState = data.block.onBlockPlaced(worldIn, pos, side, hitX, hitY, hitZ, data.meta, playerIn);
					data.meta = newState.getBlock().getMetaFromState(state);
				}

				worldIn.setBlockState(pos, data.block.getStateFromMeta(data.meta), 3);

				if(data.tileTag != null && worldIn.getTileEntity(pos) != null)
				{
					data.updateLocation(pos);
					worldIn.getTileEntity(pos).readFromNBT(data.tileTag);
				}

				if(data.block != null)
				{
					data.block.onBlockPlacedBy(worldIn, pos, state/*TODO ??*/, playerIn, new ItemStack(data.block, 1, data.meta));
					//data.block.onPostBlockPlaced(worldIn, x, y, z, data.meta);
				}

				float motion = 0.7F;
				double motionX = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionY = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionZ = (worldIn.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

				EntityItem entityItem = new EntityItem(worldIn, pos.getX() + motionX, pos.getY() + motionY, pos.getZ() + motionZ, itemStack);

				worldIn.spawnEntityInWorld(entityItem);
			}
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityCardboardBox();
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
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		TileEntityCardboardBox tileEntity = (TileEntityCardboardBox)world.getTileEntity(pos);

		ItemStack itemStack = new ItemStack(MekanismBlocks.CardboardBox, 1, world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)));

		if(itemStack.getItemDamage() == 1)
		{
			if(tileEntity.storedData != null)
			{
				((ItemBlockCardboardBox)itemStack.getItem()).setBlockData(itemStack, tileEntity.storedData);
			}
		}

		return itemStack;
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
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return null;
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
}
