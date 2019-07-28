package mekanism.common.block.plastic;

import java.util.Locale;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockPlasticGlow extends Block implements IColoredBlock {

    private final EnumColor color;
    private final String name;

    public BlockPlasticGlow(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.dyeName.toLowerCase(Locale.ROOT) + "_plastic_glow";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 10;
    }
}