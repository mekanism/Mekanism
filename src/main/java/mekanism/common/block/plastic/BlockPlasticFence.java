package mekanism.common.block.plastic;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
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

public class BlockPlasticFence extends BlockFence implements IColoredBlock {

    private final EnumColor color;
    private final String name;

    public BlockPlasticFence(EnumColor color) {
        super(Material.CLAY, Material.CLAY.getMaterialMapColor());
        this.color = color;
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.dyeName.toLowerCase(Locale.ROOT) + "_plastic_fence";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
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
            ResourceLocation baseLocation = new ResourceLocation(Mekanism.MODID, "plastic_fence");
            return new ModelResourceLocation(baseLocation, properties);
        }
    }
}