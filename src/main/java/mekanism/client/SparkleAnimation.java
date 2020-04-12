package mekanism.client;

import java.util.concurrent.ThreadLocalRandom;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SparkleAnimation {

    private TileEntity tile;
    private BlockPos corner1;
    private BlockPos corner2;
    private INodeChecker nodeChecker;

    public SparkleAnimation(TileEntity tile, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
        this.tile = tile;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.nodeChecker = checker;
    }

    public SparkleAnimation(TileEntity tile, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
        this(tile, new BlockPos(renderLoc.getX(), renderLoc.getY() - 1, renderLoc.getZ()),
              new BlockPos(renderLoc.getX() + length, renderLoc.getY() + height - 2, renderLoc.getZ() + width - 1), checker);
    }


    public void run() {
        if (MekanismConfig.client.dynamicTankEasterEgg.get()) {
            tile.getWorld().playSound(null, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), MekanismSounds.CJ_EASTER_EGG.getSoundEvent(), SoundCategory.BLOCKS, 1F, 1F);
        }
        World world = tile.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // Using the provided radius, iterate over all the positions within the radius
        for (BlockPos pos : BlockPos.getAllInBoxMutable(corner1, corner2)) {
            TileEntity t = MekanismUtils.getTileEntity(world, pos);
            if (t == null || !nodeChecker.isNode(t)) {
                continue;
            }
            for (int i = 0; i < 2; i++) {
                world.addParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                      pos.getZ() + -.01, 0, 0, 0);
                world.addParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                      pos.getZ() + 1.01, 0, 0, 0);
                world.addParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX() + -.01, pos.getY() + random.nextDouble(),
                      pos.getZ() + random.nextDouble(), 0, 0, 0);
                world.addParticle(RedstoneParticleData.REDSTONE_DUST, pos.getX() + 1.01, pos.getY() + random.nextDouble(),
                      pos.getZ() + random.nextDouble(), 0, 0, 0);
            }
        }
    }

    public interface INodeChecker {

        boolean isNode(TileEntity tile);
    }
}