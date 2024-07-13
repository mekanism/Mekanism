package mekanism.common.entity.ai;

import java.util.EnumSet;
import mekanism.common.entity.EntityRobit;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
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
                //Math from TamableAnimal#shouldTryTeleportToOwner and tryToTeleportToOwner
                if (theRobit.distanceToSqr(target) >= 144.0) {
                    teleportToAroundBlockPos(target.blockPosition());
                } else {
                    getNavigator().moveTo(target, moveSpeed);
                }
            }
        }
    }

    /**
     * Copy of {@link net.minecraft.world.entity.TamableAnimal#teleportToAroundBlockPos(BlockPos)}
     */
    private void teleportToAroundBlockPos(BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            int j = theRobit.getRandom().nextIntBetweenInclusive(-3, 3);
            int k = theRobit.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = theRobit.getRandom().nextIntBetweenInclusive(-1, 1);
                if (maybeTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }
    }

    /**
     * Copy of {@link net.minecraft.world.entity.TamableAnimal#maybeTeleportTo(int, int, int)}
     */
    private boolean maybeTeleportTo(int x, int y, int z) {
        if (canTeleportTo(new BlockPos(x, y, z))) {
            theRobit.moveTo(x + 0.5, y, z + 0.5, theRobit.getYRot(), theRobit.getXRot());
            getNavigator().stop();
            return true;
        }
        return false;
    }

    /**
     * Copy of {@link net.minecraft.world.entity.TamableAnimal#canTeleportTo(BlockPos)}
     */
    private boolean canTeleportTo(BlockPos pos) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(theRobit, pos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        }
        BlockState blockstate = theRobit.level().getBlockState(pos.below());
        if (blockstate.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos blockpos = pos.subtract(theRobit.blockPosition());
        return theRobit.level().noCollision(theRobit, theRobit.getBoundingBox().move(blockpos));
    }
}