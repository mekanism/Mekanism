package mekanism.common.entity.ai;

import java.util.EnumSet;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

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

    protected PathNavigation getNavigator() {
        return theRobit.getNavigation();
    }

    protected Level getWorld() {
        return theRobit.level();
    }

    @Override
    public void start() {
        timeToRecalcPath = 0;
        oldWaterCost = theRobit.getPathfindingMalus(PathType.WATER);
        theRobit.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    public void stop() {
        getNavigator().stop();
        theRobit.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    protected void updateTask(Entity target) {
        theRobit.getLookControl().setLookAt(target, 6, theRobit.getMaxHeadXRot() / 10F);
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = MekanismUtils.TICKS_PER_HALF_SECOND;
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
        theRobit.moveTo(x + 0.5, y, z + 0.5, theRobit.getYRot(), theRobit.getXRot());
        getNavigator().stop();
        return true;
    }

    private boolean canNavigate(BlockPos pos) {
        Level world = getWorld();
        PathType pathnodetype = WalkNodeEvaluator.getPathTypeStatic(theRobit, pos);
        if (pathnodetype == PathType.WALKABLE) {
            BlockPos blockpos = pos.subtract(theRobit.blockPosition());
            return world.noCollision(theRobit, theRobit.getBoundingBox().move(blockpos));
        }
        return false;
    }
}