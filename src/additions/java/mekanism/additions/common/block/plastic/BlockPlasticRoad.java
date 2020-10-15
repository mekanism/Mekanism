package mekanism.additions.common.block.plastic;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BlockPlasticRoad extends BlockPlastic {

    public BlockPlasticRoad(EnumColor color) {
        super(color, properties -> properties.hardnessAndResistance(5F, 10F));
    }

    @Override
    public void onEntityWalk(@Nonnull World world, @Nonnull BlockPos pos, Entity entity) {
        double boost = 1.6;
        Vector3d motion = entity.getMotion();
        double a = Math.atan2(motion.getX(), motion.getZ());
        float slipperiness = getSlipperiness(world.getBlockState(pos), world, pos, entity);
        motion = motion.add(Math.sin(a) * boost * slipperiness, 0, Math.cos(a) * boost * slipperiness);
        entity.setMotion(motion);
    }
}