package mekanism.client;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import mekanism.common.MekanismSounds;
import mekanism.common.config.MekanismConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SparkleAnimation {

    public TileEntity pointer;

    public Random random = new Random();

    public Set<BlockPos> iteratedNodes = new HashSet<>();

    public INodeChecker nodeChecker;

    public SparkleAnimation(TileEntity tileEntity, INodeChecker checker) {
        pointer = tileEntity;
        nodeChecker = checker;
    }

    public void run() {
        try {
            if (MekanismConfig.current().general.dynamicTankEasterEgg.val()) {
                pointer.getWorld()
                      .playSound(null, pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ(),
                            MekanismSounds.CJ_EASTER_EGG, SoundCategory.BLOCKS, 1F, 1F);
            }

            loop(pointer);
        } catch (Exception e) {
        }

        try {
            new Thread(() ->
            {
                World world = pointer.getWorld();

                int count = MekanismConfig.current().client.multiblockSparkleIntensity.val();

                for (BlockPos coord : iteratedNodes) {
                    for (int i = 0; i < count; i++) {
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + random.nextDouble(),
                              coord.getY() + -.01, coord.getZ() + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + random.nextDouble(),
                              coord.getY() + 1.01, coord.getZ() + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + random.nextDouble(),
                              coord.getY() + random.nextDouble(), coord.getZ() + -.01, 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + random.nextDouble(),
                              coord.getY() + random.nextDouble(), coord.getZ() + 1.01, 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + -.01,
                              coord.getY() + random.nextDouble(), coord.getZ() + random.nextDouble(), 0, 0, 0);
                        world.spawnParticle(EnumParticleTypes.REDSTONE, coord.getX() + 1.01,
                              coord.getY() + random.nextDouble(), coord.getZ() + random.nextDouble(), 0, 0, 0);
                    }
                }
            }).start();
        } catch (Exception e) {
        }
    }

    public void loop(TileEntity tileEntity) {
        World world = pointer.getWorld();

        Deque<BlockPos> toIterate = new LinkedList<>();
        toIterate.add(tileEntity.getPos());

        while (toIterate.peekFirst() != null) {
            BlockPos testPos = toIterate.pop();
            if (iteratedNodes.contains(testPos)) {
                continue;
            }
            iteratedNodes.add(testPos);

            for (EnumFacing side : EnumFacing.VALUES) {
                BlockPos coord = testPos.offset(side);

                if (!iteratedNodes.contains(coord) && world.isBlockLoaded(coord)) {
                    TileEntity tile = world.getTileEntity(coord);

                    if (tile != null && isNode(tile)) {
                        toIterate.addLast(coord);
                    }
                }
            }
        }
    }

    public boolean isNode(TileEntity tile) {
        return nodeChecker.isNode(tile);
    }

    public interface INodeChecker {

        boolean isNode(TileEntity tile);
    }
}
