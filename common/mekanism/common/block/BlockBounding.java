package mekanism.common.block;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.common.tileentity.TileEntityBoundingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BlockBounding extends Block
{
	public BlockBounding(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getBlockTileEntity(x, y, z);
			return Block.blocksList[world.getBlockId(tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ)].onBlockActivated(world, tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ, entityplayer, facing, playerX, playerY, playerZ);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getBlockTileEntity(x, y, z);
			return Block.blocksList[world.getBlockId(tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ)].getPickBlock(target, world, tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ);
		} catch(Exception e) {
			return null;
		}
	}
	
	@Override
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		try {
			TileEntityBoundingBlock tileEntity = (TileEntityBoundingBlock)world.getBlockTileEntity(x, y, z);
			return Block.blocksList[world.getBlockId(tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ)].removeBlockByPlayer(world, player, tileEntity.mainX, tileEntity.mainY, tileEntity.mainZ);
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}
	
    @Override
    public int idDropped(int i, Random random, int j)
    {
    	return 0;
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
	public boolean renderAsNormalBlock()
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
		return new TileEntityBoundingBlock();
	}
}
