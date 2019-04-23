package mekanism.common.entity.ai;

import java.util.Iterator;
import java.util.List;
import mekanism.common.entity.EntityRobit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/*
 * 	Written by pixlepix (I'm in mekanism! Yay!)
 *	Boilerplate copied from RobitAIFollow
 */
public class RobitAIPickup extends EntityAIBase {

    /**
     * The robit entity.
     */
    private EntityRobit theRobit;

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
    private PathNavigate thePathfinder;

    /**
     * The ticker for updates.
     */
    private int ticker;

    private float oldWaterCost;

    private EntityItem closest;

    public RobitAIPickup(EntityRobit entityRobit, float speed) {
        theRobit = entityRobit;
        world = entityRobit.world;
        moveSpeed = speed;
        thePathfinder = entityRobit.getNavigator();
    }

    @Override
    public boolean shouldExecute() {
        if (!theRobit.getDropPickup()) {
            return false;
        }

        if (closest != null && closest.getDistanceSq(closest) > 100
              && thePathfinder.getPathToXYZ(closest.posX, closest.posY, closest.posZ) != null) {
            return true;
        }

        List<EntityItem> items = theRobit.world.getEntitiesWithinAABB(EntityItem.class,
              new AxisAlignedBB(theRobit.posX - 10, theRobit.posY - 10, theRobit.posZ - 10, theRobit.posX + 10,
                    theRobit.posY + 10, theRobit.posZ + 10));
        Iterator<EntityItem> iter = items.iterator();
        //Cached for slight performance
        double closestDistance = -1;

        while (iter.hasNext()) {
            EntityItem entity = iter.next();

            double distance = theRobit.getDistance(entity);

            if (distance <= 10) {
                if (closestDistance == -1 || distance < closestDistance) {
                    if (thePathfinder.getPathToXYZ(entity.posX, entity.posY, entity.posZ) != null) {
                        closest = entity;
                        closestDistance = distance;
                    }
                }
            }
        }

        //No valid items
        return closest != null && !closest.isDead;

    }

    @Override
    public boolean shouldContinueExecuting() {
        return !closest.isDead && !thePathfinder.noPath() && theRobit.getDistanceSq(closest) > 100 && theRobit
              .getDropPickup() && theRobit.getEnergy() > 0
              && closest.world.provider.getDimension() == theRobit.world.provider.getDimension();
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

    @Override
    public void updateTask() {
        if (!theRobit.getDropPickup()) {
            return;
        }

        theRobit.getLookHelper().setLookPositionWithEntity(closest, 6.0F, theRobit.getVerticalFaceSpeed() / 10);

        if (--ticker <= 0) {
            ticker = 10;

            if (!thePathfinder.tryMoveToEntityLiving(closest, moveSpeed)) {
                if (theRobit.getDistanceSq(closest) >= 144.0D) {
                    int x = MathHelper.floor(closest.posX) - 2;
                    int y = MathHelper.floor(closest.getEntityBoundingBox().minY);
                    int z = MathHelper.floor(closest.posZ) - 2;

                    for (int l = 0; l <= 4; ++l) {
                        for (int i1 = 0; i1 <= 4; ++i1) {
                            BlockPos pos = new BlockPos(x + l, y, z + i1);
                            BlockPos under = new BlockPos(x + l, y - 1, z + i1);

                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && world.getBlockState(under)
                                  .isSideSolid(world, under, EnumFacing.UP) && isEmptyBlock(pos) && isEmptyBlock(
                                  new BlockPos(x + l, y + 1, z + i1))) {
                                theRobit.setLocationAndAngles((x + l) + 0.5F, y, (z + i1) + 0.5F, theRobit.rotationYaw,
                                      theRobit.rotationPitch);
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
        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();

        return block == Blocks.AIR || !iblockstate.isFullCube();
    }
}