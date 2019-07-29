package mekanism.common.block.plastic;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPlasticRoad extends Block implements IColoredBlock {

    private final EnumColor color;
    private final String name;

    public BlockPlasticRoad(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = color.registry_prefix + "_plastic_road";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public EnumColor getColor() {
        return color;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        double boost = 1.6;
        double a = Math.atan2(entityIn.motionX, entityIn.motionZ);
        entityIn.motionX += Math.sin(a) * boost * slipperiness;
        entityIn.motionZ += Math.cos(a) * boost * slipperiness;
    }
}