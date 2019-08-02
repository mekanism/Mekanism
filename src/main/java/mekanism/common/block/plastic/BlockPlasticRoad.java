package mekanism.common.block.plastic;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPlasticRoad extends Block implements IColoredBlock, IBlockOreDict {

    private final EnumColor color;

    public BlockPlasticRoad(EnumColor color) {
        super(Material.WOOD);
        this.color = color;
        setHardness(5F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, color.registry_prefix + "_plastic_road"));
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

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        entries.add("blockPlasticRoad");
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            entries.add("blockPlasticRoad" + color.dyeName);
        }
        return entries;
    }
}