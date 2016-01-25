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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockObsidianTNT extends Block
{
	public BlockObsidianTNT()
	{
		super(Material.tnt);
		setCreativeTab(Mekanism.tabMekanism);
	}

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
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
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
			EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
			entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
			world.spawnEntityInWorld(entity);
		}
	}

	public void explode(World world, BlockPos pos)
	{
		if(!world.isRemote)
		{
			EntityObsidianTNT entity = new EntityObsidianTNT(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
			world.spawnEntityInWorld(entity);
			world.playSoundAtEntity(entity, "game.tnt.primed", 1.0F, 1.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == Items.flint_and_steel)
		{
			explode(world, pos);
			world.setBlockToAir(pos);
			return true;
		}
		else {
			return super.onBlockActivated(world, pos, state, entityplayer, side, hitX, hitY, hitZ);
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
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
