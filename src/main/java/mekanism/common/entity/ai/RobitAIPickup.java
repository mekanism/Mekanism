package mekanism.common.entity.ai;

import java.util.Iterator;
import java.util.List;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class RobitAIPickup extends RobitAIBase {

    private ItemEntity closest;

    public RobitAIPickup(EntityRobit entityRobit, float speed) {
        super(entityRobit, speed);
    }

    @Override
    public boolean shouldExecute() {
        if (!theRobit.getDropPickup()) {
            return false;
        } else if (closest != null && closest.getDistanceSq(closest) > 100 && thePathfinder.getPathToEntity(closest, 0) != null) {
            return true;
        }
        //TODO: Look at and potentially mimic the way piglins search for items to pickup once their AI has mappings
        List<ItemEntity> items = theRobit.world.getEntitiesWithinAABB(ItemEntity.class,
              new AxisAlignedBB(theRobit.getPosX() - 10, theRobit.getPosY() - 10, theRobit.getPosZ() - 10,
                    theRobit.getPosX() + 10, theRobit.getPosY() + 10, theRobit.getPosZ() + 10));
        Iterator<ItemEntity> iter = items.iterator();
        //Cached for slight performance
        double closestDistance = -1;
        while (iter.hasNext()) {
            ItemEntity entity = iter.next();
            double distance = theRobit.getDistance(entity);
            if (distance <= 10) {
                if (closestDistance == -1 || distance < closestDistance) {
                    if (thePathfinder.getPathToEntity(entity, 0) != null) {
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
        return closest.isAlive() && !thePathfinder.noPath() && theRobit.getDistanceSq(closest) > 100 && theRobit.getDropPickup() &&
               !theRobit.getEnergyContainer().isEmpty() && closest.world.getDimensionKey() == theRobit.world.getDimensionKey();
    }

    @Override
    public void tick() {
        if (theRobit.getDropPickup()) {
            updateTask(closest);
        }
    }
}