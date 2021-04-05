package mekanism.common.entity.ai;

import java.util.EnumSet;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class RobitAIBase extends Goal {

    /**
     * The robit entity.
     */
    protected final EntityRobit theRobit;

    /**
     * How fast the robit can travel.
     */
    protected final float moveSpeed;

    /**
     * The ticker for updates.
     */
    private int timeToRecalcPath;
    private float oldWaterCost;

    protected RobitAIBase(EntityRobit entityRobit, float speed) {
        theRobit = entityRobit;
        moveSpeed = speed;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected PathNavigator getNavigator() {
        return theRobit.getNavigation();
    }

    protected World getWorld() {
        return theRobit.getCommandSenderWorld();
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = theRobit.getPathfindingMalus(PathNodeType.WATER);
        theRobit.setPathfindingMalus(PathNodeType.WATER, 0);
    }

    @Override
    public void stop() {
        getNavigator().stop();
        theRobit.setPathfindingMalus(PathNodeType.WATER, oldWaterCost);
    }

    protected void updateTask(Entity target) {
        theRobit.getLookControl().setLookAt(target, 6, theRobit.getMaxHeadXRot() / 10F);
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = 10;
            if (!theRobit.isPassenger()) {
                if (theRobit.distanceToSqr(target) >= 144.0) {
                    BlockPos targetPos = target.blockPosition();
                    for (int i = 0; i < 10; i++) {
                        if (tryPathTo(target, targetPos.getX() + randomize(-3, 3), targetPos.getY() + randomize(-1, 1), targetPos.getZ() + randomize(-3, 3))) {
                            return;
                        }
                    }
                } else {
                    getNavigator().moveTo(target, moveSpeed);
                }
            }
        }
    }

    private int randomize(int min, int max) {
        return theRobit.getRandom().nextInt(max - min + 1) + min;
    }

    private boolean tryPathTo(Entity target, int x, int y, int z) {
        if (Math.abs(x - target.getX()) < 2 && Math.abs(z - target.getZ()) < 2 || !canNavigate(new BlockPos(x, y, z))) {
            return false;
        }
        theRobit.moveTo(x + 0.5, y, z + 0.5, theRobit.yRot, theRobit.xRot);
        getNavigator().stop();
        return true;
    }

    private boolean canNavigate(BlockPos pos) {
        World world = getWorld();
        PathNodeType pathnodetype = WalkNodeProcessor.getBlockPathTypeStatic(world, pos.mutable());
        if (pathnodetype == PathNodeType.WALKABLE) {
            BlockPos blockpos = pos.subtract(theRobit.blockPosition());
            return world.noCollision(theRobit, theRobit.getBoundingBox().move(blockpos));
        }
        return false;
    }
}