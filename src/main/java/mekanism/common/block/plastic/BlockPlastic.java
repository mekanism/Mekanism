package mekanism.common.block.plastic;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStatePlastic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.ResourceLocation;

public class BlockPlastic extends Block {

    private final EnumColor color;
    private final String name;

    public BlockPlastic(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.dyeName.toLowerCase(Locale.ROOT) + "_plastic";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStatePlastic(this);
    }
}