package mekanism.api.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.fml.LogicalSide;

/**
 * Base Mekanism extension of the {@link EntityTeleportEvent}.
 *
 * @since 10.3.9
 */
public class MekanismTeleportEvent extends EntityTeleportEvent {

    /**
     * @param entity  Entity teleporting.
     * @param targetX Destination x position.
     * @param targetY Destination y position.
     * @param targetZ Destination z position.
     */
    protected MekanismTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
        super(entity, targetX, targetY, targetZ);
    }

    /**
     * This event is fired before a player teleports using the Meka-Tool's Teleportation Unit.
     * <br>
     * This event is Cancelable.
     * <br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event <strong>does not</strong> allow changing the target position.
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.
     */
    public static class MekaTool extends MekanismTeleportEvent {

        private final BlockHitResult targetBlock;
        private final ItemStack mekaTool;


        /**
         * @param player      Player teleporting using the Meka-Tool.
         * @param targetX     Destination x position.
         * @param targetY     Destination y position.
         * @param targetZ     Destination z position.
         * @param mekaTool    Meka-Tool used for teleportation.
         * @param targetBlock The hit result representing the target block.
         */
        public MekaTool(Player player, double targetX, double targetY, double targetZ, ItemStack mekaTool, BlockHitResult targetBlock) {
            super(player, targetX, targetY, targetZ);
            this.mekaTool = mekaTool;
            this.targetBlock = targetBlock;
        }

        @Override
        public Player getEntity() {
            return (Player) super.getEntity();
        }

        /**
         * @return The ItemStack for the Meka-Tool the player is using to teleport.
         */
        public ItemStack getMekaTool() {
            return mekaTool;
        }

        /**
         * Gets the hit result representing the targeted block. This result will have different values than the values returned by {@link #getTarget()} as that method
         * represents the adjusted position that the player is being teleported to, rather than the block that was targeted.
         *
         * @return The hit result representing the target block for this event.
         */
        public BlockHitResult getTargetBlock() {
            return targetBlock;
        }
    }
}