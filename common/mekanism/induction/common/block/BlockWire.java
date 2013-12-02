package mekanism.induction.common.block;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.induction.common.MekanismInduction;
import mekanism.induction.common.tileentity.TileEntityWire;
import mekanism.induction.common.wire.EnumWireMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.block.IConductor;
import universalelectricity.prefab.block.BlockConductor;

/**
 * A copper wire block that can change its collision bounds based on the connection.
 * 
 * @author Calclavia, Aidancbrady
 */
public class BlockWire extends BlockConductor
{
	public BlockWire(int id)
	{
		super(Mekanism.configuration.getBlock("wire", id).getInt(id), Material.cloth);
		
		setUnlocalizedName(MekanismInduction.PREFIX + "wire");
		setStepSound(soundClothFootstep);
		setResistance(0.2F);
		setHardness(0.1f);
		setBlockBounds(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
		setCreativeTab(CreativeTabs.tabRedstone);
		Block.setBurnProperties(blockID, 30, 60);
		setTextureName(MekanismInduction.PREFIX + "wire");
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		super.onNeighborBlockChange(world, x, y, z, blockID);
		
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(tileEntity instanceof IConductor)
		{
			world.markBlockForUpdate(x, y, z);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);
		TileEntityWire tileEntity = (TileEntityWire)t;

		if(entityPlayer.getCurrentEquippedItem() != null)
		{
			if(entityPlayer.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				tileEntity.setDye(entityPlayer.getCurrentEquippedItem().getItemDamage());
				return true;
			}
			else if(entityPlayer.getCurrentEquippedItem().itemID == Block.cloth.blockID && !tileEntity.isInsulated)
			{
				tileEntity.setInsulated();
				tileEntity.setDye(BlockColored.getDyeFromBlock(entityPlayer.getCurrentEquippedItem().getItemDamage()));
				entityPlayer.inventory.decrStackSize(entityPlayer.inventory.currentItem, 1);
				return true;
			}
		}

		return false;
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
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityWire();
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	public void getSubBlocks(int i, CreativeTabs par2CreativeTabs, List list)
	{
		for(EnumWireMaterial material : EnumWireMaterial.values())
		{
			list.add(new ItemStack(i, 1, material.ordinal()));
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6)
	{
		TileEntity t = world.getBlockTileEntity(x, y, z);

		if(t instanceof TileEntityWire)
		{
			TileEntityWire tileEntity = (TileEntityWire) t;

			if(tileEntity.isInsulated)
			{
				dropBlockAsItem_do(world, x, y, z, new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(tileEntity.dyeID)));
			}
		}

		super.breakBlock(world, x, y, z, par5, par6);
	}
}