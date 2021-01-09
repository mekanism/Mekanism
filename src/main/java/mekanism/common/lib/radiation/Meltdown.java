package mekanism.common.lib.radiation;

import mekanism.common.util.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class Meltdown {

    private static final int DURATION = 100;

    private final World world;
    private final BlockPos minPos, maxPos;
    private final double magnitude, chance;

    private int ticksExisted;

    public Meltdown(World world, BlockPos minPos, BlockPos maxPos, double magnitude, double chance) {
        this.world = world;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.magnitude = magnitude;
        this.chance = chance;
    }

    public boolean update() {
        ticksExisted++;

        if (world.rand.nextInt() % 10 == 0 && world.rand.nextDouble() < magnitude * chance) {
            int x = minPos.getX() + world.rand.nextInt(maxPos.getX() - minPos.getX());
            int y = minPos.getY() + world.rand.nextInt(maxPos.getY() - minPos.getY());
            int z = minPos.getZ() + world.rand.nextInt(maxPos.getZ() - minPos.getZ());
            if (!createExplosion(world, x, y, z, 8, true, Explosion.Mode.DESTROY)) {
                //If explosion cancelled, just set the position to air to prevent infinite explosion attempts
                world.setBlockState(new BlockPos(x,y,z), Blocks.AIR.getDefaultState());
            }
        }

        if (!WorldUtils.isBlockLoaded(world, minPos) || !WorldUtils.isBlockLoaded(world, maxPos)) {
            return true;
        }

        return ticksExisted >= DURATION;
    }

    /**
     *  Copy of world.createExplosion which we can use to determine if it got cancelled by the Forge event.
     */
    private static boolean createExplosion(World worldIn, double x, double y, double z, float size, boolean causesFire, Explosion.Mode mode) {
        Explosion explosion = new Explosion(worldIn, null, null, null, x, y, z, size, causesFire, mode);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(worldIn, explosion)) return false;
        explosion.doExplosionA();
        explosion.doExplosionB(true);
        return true;
    }
}
