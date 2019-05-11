package mekanism.common.entity.ai;

import mekanism.common.entity.EntityRobit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RobitAIFollow extends EntityAIBase {

    /**
     * The robit entity.
     */
    private EntityRobit theRobit;

    /**
     * The robit's owner.
     */
    private EntityPlayer theOwner;

    /**
     * The world the robit is located in.
     */
    private World world;

    /**
     * How fast the robit can travel.
     */
    private float moveSpeed;

    /**
     * The robit's pathfinder.
     */
    private PathNavigateGround thePathfinder;

    /**
     * The ticker for updates.
     */
    private int ticker;

    /**
     * The distance between the owner the robit must be at in order for the protocol to begin.
     */
    private float maxDist;

    /**
     * The distance between the owner the robit must reach before it stops the protocol.
     */
    private float minDist;

    private float oldWaterCost;

    public RobitAIFollow(EntityRobit entityRobit, float speed, float min, float max) {
        theRobit = entityRobit;
        world = entityRobit.world;
        moveSpeed = speed;
        thePathfinder = entityRobit.getNavigator();
        minDist = min;
        maxDist = max;
    }

    @Override
    public boolean shouldExecute() {
        EntityPlayer player = theRobit.getOwner();

        if (player == null) {
            return false;
        } else if (theRobit.world.provider.getDimension() != player.world.provider.getDimension()) {
            return false;
        } else if (!theRobit.getFollowing()) {
            //Still looks up at the player if on chargepad or not following
            theRobit.getLookHelper().setLookPositionWithEntity(player, 6.0F, theRobit.getVerticalFaceSpeed() / 10);
            return false;
        } else if (theRobit.getDistanceSq(player) < (minDist * minDist)) {
            return false;
        } else if (theRobit.getEnergy() == 0) {
            return false;
        }
        theOwner = player;
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !thePathfinder.noPath() && theRobit.getDistanceSq(theOwner) > (maxDist * maxDist) && theRobit.getFollowing() && theRobit.getEnergy() > 0
               && theOwner.world.provider.getDimension() == theRobit.world.provider.getDimension();
    }

    @Override
    public void startExecuting() {
        ticker = 0;
        oldWaterCost = theRobit.getPathPriority(PathNodeType.WATER);
        theRobit.setPathPriority(PathNodeType.WATER, 0.0F);
    }

    @Override
    public void resetTask() {
        theOwner = null;
        thePathfinder.clearPath();
        theRobit.setPathPriority(PathNodeType.WATER, oldWaterCost);
    }

    @Override
    public void updateTask() {
        theRobit.getLookHelper().setLookPositionWithEntity(theOwner, 6.0F, theRobit.getVerticalFaceSpeed() / 10);
        if (theRobit.getFollowing()) {
            if (--ticker <= 0) {
                ticker = 10;

                if (!thePathfinder.tryMoveToEntityLiving(theOwner, moveSpeed)) {
                    if (theRobit.getDistanceSq(theOwner) >= 144.0D) {
                        int x = MathHelper.floor(theOwner.posX) - 2;
                        int y = MathHelper.floor(theOwner.getEntityBoundingBox().minY);
                        int z = MathHelper.floor(theOwner.posZ) - 2;
                        for (int l = 0; l <= 4; ++l) {
                            for (int i1 = 0; i1 <= 4; ++i1) {
                                BlockPos pos = new BlockPos(x + l, y, z + i1);
                                BlockPos under = new BlockPos(x + l, y - 1, z + i1);
                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && world.getBlockState(under).isSideSolid(world, under, EnumFacing.UP) && isEmptyBlock(pos) &&
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
    }

    private boolean isEmptyBlock(BlockPos pos) {
        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block == Blocks.AIR || !iblockstate.isFullCube();
    }
}