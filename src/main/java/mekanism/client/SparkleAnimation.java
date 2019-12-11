package mekanism.client;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import mekanism.common.MekanismSounds;
import mekanism.common.config.MekanismConfig;
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

    public SparkleAnimation(TileEntity tileEntity, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
        this.tile = tileEntity;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.nodeChecker = checker;
    }

    public SparkleAnimation(TileEntity tileEntity, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
        this.tile = tileEntity;
        this.corner1 = new BlockPos(renderLoc.getX(), renderLoc.getY() - 1, renderLoc.getZ());
        this.corner2 = new BlockPos(renderLoc.getX() + length, renderLoc.getY() + height - 2, renderLoc.getZ() + width - 1);
        this.nodeChecker = checker;
    }


    public void run() {
        if (MekanismConfig.general.dynamicTankEasterEgg.get()) {
            tile.getWorld().playSound(null, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), MekanismSounds.CJ_EASTER_EGG.getSoundEvent(), SoundCategory.BLOCKS, 1F, 1F);
        }

        // Using the provided radius, get an iterable over all the positions within the radius
        Iterator<BlockPos> itr = BlockPos.getAllInBoxMutable(corner1, corner2).iterator();

        World world = tile.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (itr.hasNext()) {
            BlockPos pos = itr.next();
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