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

public class BlockPlasticReinforced extends Block {

    private final EnumColor color;
    private final String name;

    public BlockPlasticReinforced(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(50F);
        setResistance(2000F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.dyeName.toLowerCase(Locale.ROOT) + "_plastic_reinforced";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStatePlastic(this);
    }
}