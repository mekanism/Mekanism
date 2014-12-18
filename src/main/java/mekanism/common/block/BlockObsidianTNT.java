package mekanism.common.block;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.tile.TileEntityObsidianTNT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockObsidianTNT extends Block
{
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[256];

	public BlockObsidianTNT()
	{
		super(Material.tnt);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister register) {}
*/

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(world, pos, state);

		if(world.isBlockIndirectlyGettingPowered(pos) > 0)
		{
			explode(world, pos);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess blockAccess, BlockPos pos, BlockPos neighbor)
	{
		if(!(blockAccess instanceof World))
		{
			return;
		}

		World world = (World)blockAccess;

		if(world.isBlockIndirectlyGettingPowered(pos) > 0)
		{
			explode(world, pos);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion)
	{
		if(!world.isRemote)
		{
			EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ()+ 0.5F);
			entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
			world.spawnEntityInWorld(entity);
		}
	}

	public void explode(World world, BlockPos pos)
	{
		if(!world.isRemote)
		{
			EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ()+ 0.5F);
			world.spawnEntityInWorld(entity);
			world.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(playerIn.getCurrentEquippedItem() != null && playerIn.getCurrentEquippedItem().getItem() == Items.flint_and_steel)
		{
			explode(worldIn, pos);
			worldIn.setBlockToAir(pos);
			return true;
		}
		else {
			return super.onBlockActivated(worldIn, pos, state, playerIn, side, hitX, hitY, hitZ);
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion)
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
		return new TileEntityObsidianTNT();
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
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity)
	{
		if(entity instanceof EntityArrow && !world.isRemote)
		{
			EntityArrow entityarrow = (EntityArrow)entity;

			if(entityarrow.isBurning())
			{
				explode(world, pos);
				world.setBlockToAir(pos);
			}
		}
	}
}
