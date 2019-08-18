package mekanism.common.block.plastic;

import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockPlasticRoad extends Block implements IColoredBlock {

    private final EnumColor color;

    public BlockPlasticRoad(EnumColor color) {
        super(Block.Properties.create(Material.WOOD).hardnessAndResistance(5F, 10F));
        this.color = color;
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
}