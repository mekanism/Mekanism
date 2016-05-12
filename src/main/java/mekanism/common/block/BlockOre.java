package mekanism.common.block;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateOre;
import mekanism.common.block.states.BlockStateOre.EnumOreType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple ore block IDs.
 * 0: Osmium Ore
 * 1: Copper Ore
 * 2: Tin Ore
 * @author AidanBrady
 *
 */
public class BlockOre extends Block
{
	public BlockOre()
	{
		super(Material.rock);
		setHardness(3F);
		setResistance(5F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockStateOre(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(BlockStateOre.typeProperty, EnumOreType.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(BlockStateOre.typeProperty).ordinal();
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
	{
		for(EnumOreType ore : EnumOreType.values())
		{
			list.add(new ItemStack(item, 1, ore.ordinal()));
		}
	}
}
