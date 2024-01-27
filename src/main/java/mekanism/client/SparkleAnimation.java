package mekanism.client;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class SparkleAnimation {

    private final BlockEntity tile;
    private final BlockPos corner1;
    private final BlockPos corner2;

    public SparkleAnimation(BlockEntity tile, BlockPos corner1, BlockPos corner2) {
        this.tile = tile;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public SparkleAnimation(BlockEntity tile, BlockPos renderLoc, int length, int width, int height) {
        this(tile, new BlockPos(renderLoc.getX(), renderLoc.getY() - 1, renderLoc.getZ()),
              new BlockPos(renderLoc.getX() + length, renderLoc.getY() + height - 1, renderLoc.getZ() + width));
    }

    public void run() {
        Level world = tile.getLevel();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int xSize = corner2.getX() - corner1.getX() + 1, ySize = corner2.getY() - corner1.getY() + 1, zSize = corner2.getZ() - corner1.getZ() + 1;
        Vec3 origin = new Vec3(xSize / 2D, ySize / 2D, zSize / 2D);
        Vec3 displacement = origin;
        sparkleSide(world, random, origin, displacement, xSize, ySize, 0, 0);
        sparkleSide(world, random, origin, displacement, xSize, ySize, Mth.PI, 0);
        displacement = new Vec3(origin.z, origin.y, origin.x);
        sparkleSide(world, random, origin, displacement, zSize, ySize, Mth.HALF_PI, 0);
        sparkleSide(world, random, origin, displacement, zSize, ySize, 3 * Mth.HALF_PI, 0);
        displacement = new Vec3(origin.x, origin.z, origin.y);
        sparkleSide(world, random, origin, displacement, xSize, zSize, 0, Mth.HALF_PI);
        sparkleSide(world, random, origin, displacement, xSize, zSize, 0, 3 * Mth.HALF_PI);
    }

    private void sparkleSide(Level world, Random random, Vec3 origin, Vec3 displacement, int width, int height, float rotationYaw, float rotationPitch) {
        for (int i = 0; i < 100; i++) {
            Vec3 pos = new Vec3(width * random.nextDouble(), height * random.nextDouble(), -0.01).subtract(displacement);
            pos = pos.yRot(rotationYaw).xRot(rotationPitch);
            pos = pos.add(origin).add(corner1.getX(), corner1.getY(), corner1.getZ());
            world.addParticle(DustParticleOptions.REDSTONE, pos.x(), pos.y(), pos.z(), 0, 0, 0);
        }
    }
}