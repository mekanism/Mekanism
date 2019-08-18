package mekanism.common.entity.ai;

import java.util.Iterator;
import java.util.List;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;

/*
 * 	Written by pixlepix (I'm in mekanism! Yay!)
 *	Boilerplate copied from RobitAIFollow
 */
public class RobitAIPickup extends RobitAIBase {

    private ItemEntity closest;

    public RobitAIPickup(EntityRobit entityRobit, float speed) {
        super(entityRobit, speed);
    }

    @Override
    public boolean shouldExecute() {
        if (!theRobit.getDropPickup()) {
            return false;
        }
        //TODO: Check if pathing is correct and what the param is for
        if (closest != null && closest.getDistanceSq(closest) > 100 && thePathfinder.getPathToEntityLiving(closest, 0) != null) {
            return true;
        }
        List<ItemEntity> items = theRobit.world.getEntitiesWithinAABB(ItemEntity.class,
              new AxisAlignedBB(theRobit.posX - 10, theRobit.posY - 10, theRobit.posZ - 10, theRobit.posX + 10, theRobit.posY + 10, theRobit.posZ + 10));
        Iterator<ItemEntity> iter = items.iterator();
        //Cached for slight performance
        double closestDistance = -1;

        while (iter.hasNext()) {
            ItemEntity entity = iter.next();
            double distance = theRobit.getDistance(entity);
            if (distance <= 10) {
                if (closestDistance == -1 || distance < closestDistance) {
                    //TODO: Check if pathing is correct and what the param is for
                    if (thePathfinder.getPathToEntityLiving(entity, 0) != null) {
                        closest = entity;
                        closestDistance = distance;
                    }
                }
            }
        }
        //No valid items
        return closest != null && closest.isAlive();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return closest.isAlive() && !thePathfinder.noPath() && theRobit.getDistanceSq(closest) > 100 && theRobit.getDropPickup() && theRobit.getEnergy() > 0
               && closest.world.getDimension().equals(theRobit.world.getDimension());
    }

    @Override
    public void tick() {
        if (theRobit.getDropPickup()) {
            updateTask(closest);
        }
    }
}