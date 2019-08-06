package mekanism.common.entity.ai;

import mekanism.common.entity.EntityRobit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class RobitAIBase extends EntityAIBase {

    /**
     * The robit entity.
     */
    protected EntityRobit theRobit;

    /**
     * The world the robit is located in.
     */
    protected World world;

    /**
     * How fast the robit can travel.
     */
    protected float moveSpeed;

    /**
     * The robit's pathfinder.
     */
    protected PathNavigateGround thePathfinder;

    /**
     * The ticker for updates.
     */
    protected int ticker;

    protected float oldWaterCost;

    protected RobitAIBase(EntityRobit entityRobit, float speed) {
        theRobit = entityRobit;
        world = entityRobit.world;
        moveSpeed = speed;
        thePathfinder = entityRobit.getNavigator();
    }

    @Override
    public void startExecuting() {
        ticker = 0;
        oldWaterCost = theRobit.getPathPriority(PathNodeType.WATER);
        theRobit.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void resetTask() {
        thePathfinder.clearPath();
        theRobit.setPathPriority(PathNodeType.WATER, oldWaterCost);
    }

    protected void updateTask(Entity target) {
        theRobit.getLookHelper().setLookPositionWithEntity(target, 6.0F, theRobit.getVerticalFaceSpeed() / 10);
        if (--ticker <= 0) {
            ticker = 10;
            if (!thePathfinder.tryMoveToEntityLiving(target, moveSpeed)) {
                if (theRobit.getDistanceSq(target) >= 144.0D) {
                    int x = MathHelper.floor(target.posX) - 2;
                    int y = MathHelper.floor(target.getEntityBoundingBox().minY);
                    int z = MathHelper.floor(target.posZ) - 2;
                    for (int l = 0; l <= 4; ++l) {
                        for (int i1 = 0; i1 <= 4; ++i1) {
                            BlockPos pos = new BlockPos(x + l, y, z + i1);
                            BlockPos under = new BlockPos(x + l, y - 1, z + i1);
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && world.getBlockState(under).isSideSolid(world, under, Direction.UP) && isEmptyBlock(pos) &&
                                isEmptyBlock(new BlockPos(x + l, y + 1, z + i1))) {
                                theRobit.setLocationAndAngles((x + l) + 0.5F, y, (z + i1) + 0.5F, theRobit.rotationYaw, theRobit.rotationPitch);
                                thePathfinder.clearPath();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isEmptyBlock(BlockPos pos) {
        BlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block == Blocks.AIR || !iblockstate.isFullCube();
    }
}