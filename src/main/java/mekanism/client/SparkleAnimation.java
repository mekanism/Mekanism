package mekanism.client;

import java.util.concurrent.ThreadLocalRandom;
import mekanism.common.MekanismSounds;
import mekanism.common.config.MekanismConfig.general;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SparkleAnimation {

    private TileEntity tile;
    private int radius;
    private INodeChecker nodeChecker;

    public SparkleAnimation(TileEntity tileEntity, int radius, INodeChecker checker) {
        this.tile = tileEntity;
        this.radius = radius;
        this.nodeChecker = checker;
    }

    public void run() {
        if (general.dynamicTankEasterEgg) {
            tile.getWorld()
                  .playSound(null, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(),
                        MekanismSounds.CJ_EASTER_EGG, SoundCategory.BLOCKS, 1F, 1F);
        }

        // Using the provided radius, get an iterable over all the positions within the radius
        BlockPos center = tile.getPos();
        Iterable<MutableBlockPos> itr = BlockPos.getAllInBoxMutable(center.getX() - radius, center.getY() + radius,
            center.getY() - radius, center.getY() + radius,
            center.getZ() - radius, center.getY() + radius);

        new Thread(() ->
        {
            World world = tile.getWorld();
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (MutableBlockPos pos : itr) {
                TileEntity t = world.getTileEntity(pos);
                if (!nodeChecker.isNode(t)) {
                    continue;
                }

                for (int i = 0; i < 6; i++) {
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + random.nextDouble(),
                          pos.getY() + -.01, pos.getZ() + random.nextDouble(), 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + random.nextDouble(),
                          pos.getY() + 1.01, pos.getZ() + random.nextDouble(), 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + random.nextDouble(),
                          pos.getY() + random.nextDouble(), pos.getZ() + -.01, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + random.nextDouble(),
                          pos.getY() + random.nextDouble(), pos.getZ() + 1.01, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + -.01,
                          pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + 1.01,
                          pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
                }
            }
        }).start();
    }

    public interface INodeChecker {

        boolean isNode(TileEntity tile);
    }
}
