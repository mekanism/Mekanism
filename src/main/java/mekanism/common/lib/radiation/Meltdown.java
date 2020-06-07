package mekanism.common.lib.radiation;

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
            world.createExplosion(null,
                  minPos.getX() + world.rand.nextInt(maxPos.getX() - minPos.getX()),
                  minPos.getY() + world.rand.nextInt(maxPos.getY() - minPos.getY()),
                  minPos.getZ() + world.rand.nextInt(maxPos.getZ() - minPos.getZ()),
                  8, true, Explosion.Mode.DESTROY);
        }

        if (!world.isBlockPresent(minPos) || !world.isBlockPresent(maxPos)) {
            return true;
        }

        return ticksExisted >= DURATION;
    }
}
