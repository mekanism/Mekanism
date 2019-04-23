package mekanism.common.block;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class BlockPlasticFence extends BlockFence {

    public BlockPlasticFence() {
        super(Material.CLAY, Material.CLAY.getMaterialMapColor());
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, WEST, SOUTH, colorProperty);
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(colorProperty, EnumDyeColor.byDyeDamage(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(colorProperty).getDyeDamage();
    }

    @Override
    public void getSubBlocks(CreativeTabs creativetabs, NonNullList<ItemStack> list) {
        for (int i = 0; i < EnumColor.DYES.length; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    public static class PlasticFenceStateMapper extends StateMapperBase {

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            String properties = "east=" + state.getValue(EAST) + ",";
            properties += "north=" + state.getValue(NORTH) + ",";
            properties += "south=" + state.getValue(SOUTH) + ",";
            properties += "west=" + state.getValue(WEST);
            ResourceLocation baseLocation = new ResourceLocation("mekanism", "PlasticFence");
            return new ModelResourceLocation(baseLocation, properties);
        }
    }
}
