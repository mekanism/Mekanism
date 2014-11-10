package mekanism.common.block;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.tile.TileEntityObsidianTNT;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockObsidianTNT extends Block
{
	public IIcon[] icons = new IIcon[256];

	public BlockObsidianTNT()
	{
		super(Material.tnt);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		super.onBlockAdded(world, x, y, z);

		if(world.isBlockIndirectlyGettingPowered(x, y, z))
		{
			explode(world, x, y, z);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(world.isBlockIndirectlyGettingPowered(x, y, z))
		{
			explode(world, x, y, z);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion)
	{
		if(!world.isRemote)
		{
			EntityObsidianTNT entity = new EntityObsidianTNT(world, x + 0.5F, y + 0.5F, z + 0.5F);
			entity.fuse = world.rand.nextInt(entity.fuse / 4) + entity.fuse / 8;
			world.spawnEntityInWorld(entity);
		}
	}

	public void explode(World world, int x, int y, int z)
	{
		if(!world.isRemote)
		{
			EntityObsidianTNT entity = new EntityObsidianTNT(world, x + 0.5F, y + 0.5F, z + 0.5F);
			world.spawnEntityInWorld(entity);
			world.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
	{
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == Items.flint_and_steel)
		{
			explode(world, x, y, z);
			world.setBlockToAir(x, y, z);
			return true;
		}
		else {
			return super.onBlockActivated(world, x, y, z, entityplayer, i1, f1, f2, f3);
		}
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion)
	{
		return false;
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityObsidianTNT();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

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
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if(entity instanceof EntityArrow && !world.isRemote)
		{
			EntityArrow entityarrow = (EntityArrow)entity;

			if(entityarrow.isBurning())
			{
				explode(world, x, y, z);
				world.setBlockToAir(x, y, z);
			}
		}
	}
}
