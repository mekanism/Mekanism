package mekanism.client;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import mekanism.common.Mekanism;
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
        if (general.dynamicTankEasterEgg) {
            tile.getWorld()
                  .playSound(null, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(),
                        MekanismSounds.CJ_EASTER_EGG, SoundCategory.BLOCKS, 1F, 1F);
        }

        // Using the provided radius, get an iterable over all the positions within the radius
        Iterator<MutableBlockPos> itr = BlockPos.getAllInBoxMutable(corner1, corner2).iterator();

        World world = tile.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (itr.hasNext()) {
            BlockPos pos = itr.next();
            if (world.isAirBlock(pos)) {
                continue;
            }

            TileEntity t = world.getTileEntity(pos);
            if (t == null || !nodeChecker.isNode(t)) {
                continue;
            }

            for (int i = 0; i < 2; i++) {
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
    }

    public interface INodeChecker {
        boolean isNode(TileEntity tile);
    }
}
