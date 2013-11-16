package mekanism.induction.common.block;

import mekanism.induction.common.tileentity.TileEntityAdvancedFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author Calclavia
 * 
 */
public class BlockAdvancedFurnace extends BlockFurnace
{
	protected BlockAdvancedFurnace(int id, boolean isBurning)
	{
		super(id, isBurning);
		this.setHardness(3.5F);
		this.setStepSound(soundStoneFootstep);
		this.setUnlocalizedName("furnace");

		if (isBurning)
		{
			this.setLightValue(0.875F);
		}
		else
		{
			this.setCreativeTab(CreativeTabs.tabDecorations);
		}
	}

	public static BlockAdvancedFurnace createNew(boolean isBurning)
	{
		int id = Block.furnaceIdle.blockID;

		if (isBurning)
		{
			id = Block.furnaceBurning.blockID;
		}

		Block.blocksList[id] = null;
		Item.itemsList[id] = null;
		return new BlockAdvancedFurnace(id, isBurning);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int newID)
	{
		super.onNeighborBlockChange(world, x, y, z, newID);
		((TileEntityAdvancedFurnace)world.getBlockTileEntity(x, y, z)).checkProduce();
	}

	@Override
	public TileEntity createNewTileEntity(World par1World)
	{
		return new TileEntityAdvancedFurnace();
	}
}
