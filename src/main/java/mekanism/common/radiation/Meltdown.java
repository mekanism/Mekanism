package mekanism.common.radiation;

import mekanism.api.Coord4D;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class Meltdown {

    private static final int DURATION = 100;

    private World world;
    private Coord4D minPos, maxPos;
    private double magnitude, chance;

    private int ticksExisted;

    public Meltdown(World world, Coord4D minPos, Coord4D maxPos, double magnitude, double chance) {
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
                minPos.x + world.rand.nextInt(maxPos.x - minPos.x),
                minPos.y + world.rand.nextInt(maxPos.y - minPos.y),
                minPos.z + world.rand.nextInt(maxPos.z - minPos.z),
                6, true, Explosion.Mode.DESTROY);
        }

        if (!world.isBlockPresent(minPos.getPos()) || !world.isBlockPresent(maxPos.getPos())) {
            return true;
        }

        return ticksExisted >= DURATION;
    }
}
