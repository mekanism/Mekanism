package mekanism.common.entity.ai;

import java.util.List;
import java.util.function.Predicate;
import mekanism.common.entity.EntityRobit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.AxisAlignedBB;

public class RobitAIPickup extends RobitAIBase {

    private static final int SEARCH_RADIUS = 10;
    private static final int SEARCH_RADIUS_SQ = SEARCH_RADIUS * SEARCH_RADIUS;

    private final Predicate<Entity> itemPredicate = entity -> !entity.isSpectator() && entity instanceof ItemEntity && theRobit.isItemValid((ItemEntity) entity);
    private ItemEntity closest;

    public RobitAIPickup(EntityRobit entityRobit, float speed) {
        super(entityRobit, speed);
    }

    @Override
    public boolean shouldExecute() {
        if (!theRobit.getDropPickup()) {
            return false;
        }
        PathNavigator navigator = getNavigator();
        if (validateClosest() && navigator.pathfind(closest, 0) != null) {
            return true;
        }
        //Ensure we don't have a closest one set
        closest = null;
        //Cached for slight performance
        double closestDistance = -1;
        //TODO: Look at and potentially mimic the way piglins search for items to pickup once their AI has mappings
        List<ItemEntity> items = theRobit.world.getEntitiesWithinAABB(ItemEntity.class,
              new AxisAlignedBB(theRobit.getPosX() - SEARCH_RADIUS, theRobit.getPosY() - SEARCH_RADIUS, theRobit.getPosZ() - SEARCH_RADIUS,
                    theRobit.getPosX() + SEARCH_RADIUS, theRobit.getPosY() + SEARCH_RADIUS, theRobit.getPosZ() + SEARCH_RADIUS), itemPredicate);
        for (ItemEntity entity : items) {
            double distance = theRobit.getDistanceSq(entity);
            if (distance <= SEARCH_RADIUS_SQ) {
                if (closestDistance == -1 || distance < closestDistance) {
                    if (navigator.pathfind(entity, 0) != null) {
                        closest = entity;
                        closestDistance = distance;
                    }
                }
            }
        }
        //No valid items
        return closest != null;
    }

    private boolean validateClosest() {
        return closest != null && theRobit.isItemValid(closest) && closest.world.getDimensionKey() == theRobit.world.getDimensionKey() &&
               theRobit.getDistanceSq(closest) <= SEARCH_RADIUS_SQ;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return theRobit.getDropPickup() && validateClosest() && !getNavigator().noPath() && !theRobit.getEnergyContainer().isEmpty();
    }

    @Override
    public void tick() {
        if (theRobit.getDropPickup()) {
            updateTask(closest);
        }
    }
}