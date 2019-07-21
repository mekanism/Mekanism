package mekanism.common.block.plastic;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStatePlastic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPlasticRoad extends Block {

    private final EnumColor color;
    private final String name;

    public BlockPlasticRoad(EnumColor color) {
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

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        double boost = 1.6;
        double a = Math.atan2(entityIn.motionX, entityIn.motionZ);
        entityIn.motionX += Math.sin(a) * boost * slipperiness;
        entityIn.motionZ += Math.cos(a) * boost * slipperiness;
    }
}