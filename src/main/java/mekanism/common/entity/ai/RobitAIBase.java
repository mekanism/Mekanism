package mekanism.common.entity.ai;

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
     * The world the robit is located in.
     */
    protected final World world;

    /**
     * How fast the robit can travel.
     */
    protected final float moveSpeed;

    /**
     * The robit's pathfinder.
     */
    protected final PathNavigator thePathfinder;

    /**
     * The ticker for updates.
     */
    protected int timeToRecalcPath;

    protected float oldWaterCost;

    protected RobitAIBase(EntityRobit entityRobit, float speed) {
        theRobit = entityRobit;
        world = entityRobit.world;
        moveSpeed = speed;
        thePathfinder = entityRobit.getNavigator();
    }

    @Override
    public void startExecuting() {
        timeToRecalcPath = 0;
        oldWaterCost = theRobit.getPathPriority(PathNodeType.WATER);
        theRobit.setPathPriority(PathNodeType.WATER, 0);
    }

    @Override
    public void resetTask() {
        thePathfinder.clearPath();
        theRobit.setPathPriority(PathNodeType.WATER, oldWaterCost);
    }

    protected void updateTask(Entity target) {
        theRobit.getLookController().setLookPositionWithEntity(target, 6, theRobit.getVerticalFaceSpeed() / 10F);
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = 10;
            if (!theRobit.isPassenger()) {
                if (theRobit.getDistanceSq(target) >= 144.0) {
                    BlockPos targetPos = target.getPosition();
                    for (int i = 0; i < 10; i++) {
                        if (tryPathTo(target, targetPos.getX() + randomize(-3, 3), targetPos.getY() + randomize(-1, 1), targetPos.getZ() + randomize(-3, 3))) {
                            return;
                        }
                    }
                } else {
                    thePathfinder.tryMoveToEntityLiving(target, moveSpeed);
                }
            }
        }
    }

    private int randomize(int min, int max) {
        return theRobit.getRNG().nextInt(max - min + 1) + min;
    }

    private boolean tryPathTo(Entity target, int x, int y, int z) {
        if (Math.abs(x - target.getPosX()) < 2 && Math.abs(z - target.getPosZ()) < 2 || !canNavigate(new BlockPos(x, y, z))) {
            return false;
        }
        theRobit.setLocationAndAngles(x + 0.5, y, z + 0.5, theRobit.rotationYaw, theRobit.rotationPitch);
        thePathfinder.clearPath();
        return true;
    }

    private boolean canNavigate(BlockPos pos) {
        PathNodeType pathnodetype = WalkNodeProcessor.func_237231_a_(this.world, pos.toMutable());
        if (pathnodetype == PathNodeType.WALKABLE) {
            BlockPos blockpos = pos.subtract(theRobit.getPosition());
            return world.hasNoCollisions(theRobit, theRobit.getBoundingBox().offset(blockpos));
        }
        return false;
    }
}