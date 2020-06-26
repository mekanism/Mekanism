package mekanism.client;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SparkleAnimation {

    private final TileEntity tile;
    private final BlockPos corner1;
    private final BlockPos corner2;

    public SparkleAnimation(TileEntity tile, BlockPos corner1, BlockPos corner2) {
        this.tile = tile;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public SparkleAnimation(TileEntity tile, BlockPos renderLoc, int length, int width, int height) {
        this(tile, new BlockPos(renderLoc.getX(), renderLoc.getY() - 1, renderLoc.getZ()),
              new BlockPos(renderLoc.getX() + length, renderLoc.getY() + height - 1, renderLoc.getZ() + width));
    }

    public void run() {
        World world = tile.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int xSize = corner2.getX() - corner1.getX() + 1, ySize = corner2.getY() - corner1.getY() + 1, zSize = corner2.getZ() - corner1.getZ() + 1;
        Vector3d origin = new Vector3d(xSize / 2D, ySize / 2D, zSize / 2D);
        Vector3d displacement = origin;
        sparkleSide(world, random, origin, displacement, xSize, ySize, 0, 0);
        sparkleSide(world, random, origin, displacement, xSize, ySize, (float) Math.PI, 0);
        displacement = new Vector3d(origin.z, origin.y, origin.x);
        sparkleSide(world, random, origin, displacement, zSize, ySize, (float) Math.PI / 2, 0);
        sparkleSide(world, random, origin, displacement, zSize, ySize, (float) (3 * Math.PI) / 2, 0);
        displacement = new Vector3d(origin.x, origin.z, origin.y);
        sparkleSide(world, random, origin, displacement, xSize, zSize, 0, (float) Math.PI / 2);
        sparkleSide(world, random, origin, displacement, xSize, zSize, 0, (float) (3 * Math.PI) / 2);
    }

    private void sparkleSide(World world, Random random, Vector3d origin, Vector3d displacement, int width, int height, float rotationYaw, float rotationPitch) {
        for (int i = 0; i < 100; i++) {
            Vector3d pos = new Vector3d(width * random.nextDouble(), height * random.nextDouble(), -0.01).subtract(displacement);
            pos = pos.rotateYaw(rotationYaw).rotatePitch(rotationPitch);
            pos = pos.add(origin).add(corner1.getX(), corner1.getY(), corner1.getZ());
            world.addParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
        }
    }
}