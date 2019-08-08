package mekanism.common.block.plastic;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
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
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        double boost = 1.6;
        Vec3d motion = entity.getMotion();
        double a = Math.atan2(motion.getX(), motion.getZ());
        float slipperiness = getSlipperiness(world.getBlockState(pos), world, pos, entity);
        motion = motion.add(Math.sin(a) * boost * slipperiness, 0, Math.cos(a) * boost * slipperiness);
        entity.setMotion(motion);
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