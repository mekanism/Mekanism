package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStateOre;
import mekanism.common.block.states.BlockStateOre.EnumOreType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * Block class for handling multiple ore block IDs. 0: Osmium Ore 1: Copper Ore 2: Tin Ore
 *
 * @author AidanBrady
 */
public class BlockOre extends Block {

    public BlockOre() {
        super(Material.ROCK);
        setHardness(3F);
        setResistance(5F);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateOre(this);
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockStateOre.typeProperty, EnumOreType.values()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockStateOre.typeProperty).ordinal();
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (EnumOreType ore : EnumOreType.values()) {
            list.add(new ItemStack(this, 1, ore.ordinal()));
        }
    }
}
