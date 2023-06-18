package mekanism.common.entity.ai;

import mekanism.common.entity.EntityRobit;
import net.minecraft.world.entity.player.Player;

public class RobitAIFollow extends RobitAIBase {

    /**
     * The Robit's owner.
     */
    private Player theOwner;
    /**
     * The distance between the owner the robit must be at in order for the protocol to begin.
     */
    private final float maxDist;
    /**
     * The distance between the owner the robit must reach before it stops the protocol.
     */
    private final float minDist;

    public RobitAIFollow(EntityRobit entityRobit, float speed, float min, float max) {
        super(entityRobit, speed);
        minDist = min;
        maxDist = max;
    }

    @Override
    public boolean canUse() {
        Player player = theRobit.getOwner();
        if (player == null || player.isSpectator()) {
            return false;
        } else if (theRobit.level().dimension() != player.level().dimension()) {
            return false;
        } else if (!theRobit.getFollowing()) {
            //Still looks up at the player if on chargepad or not following
            theRobit.getLookControl().setLookAt(player, 6, theRobit.getMaxHeadXRot() / 10F);
            return false;
        } else if (theRobit.distanceToSqr(player) < (minDist * minDist)) {
            return false;
        } else if (theRobit.getEnergyContainer().isEmpty()) {
            return false;
        }
        theOwner = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !getNavigator().isDone() && theRobit.distanceToSqr(theOwner) > (maxDist * maxDist) && theRobit.getFollowing() &&
               !theRobit.getEnergyContainer().isEmpty() && theOwner.level().dimension() == theRobit.level().dimension();
    }

    @Override
    public void stop() {
        theOwner = null;
        super.stop();
    }

    @Override
    public void tick() {
        if (theRobit.getFollowing()) {
            updateTask(theOwner);
        }
    }
}