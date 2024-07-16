package mekanism.api.event;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.robit.IRobit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/**
 * Base Mekanism extension of the {@link EntityTeleportEvent}.
 *
 * @since 10.3.9
 */
@NothingNullByDefault
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

    /**
     * @since 10.5.2
     */
    public static class GlobalTeleport extends MekanismTeleportEvent {

        private final ResourceKey<Level> targetDimension;

        /**
         * @param entity          The entity that is teleporting.
         * @param targetX         Destination x position.
         * @param targetY         Destination y position.
         * @param targetZ         Destination z position.
         * @param targetDimension Destination dimension.
         */
        public GlobalTeleport(Entity entity, double targetX, double targetY, double targetZ, ResourceKey<Level> targetDimension) {
            super(entity, targetX, targetY, targetZ);
            this.targetDimension = targetDimension;
        }

        /**
         * @return If the teleport is going across dimensions
         */
        public boolean isTransDimensional() {
            return getEntity().level().dimension() != targetDimension;
        }

        /**
         * Gets the dimension the entity is teleporting to.
         */
        public ResourceKey<Level> getTargetDimension() {
            return targetDimension;
        }
    }

    /**
     * This event is fired before a robit teleports back home.
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
     *
     * @since 10.5.2
     */
    public static class Robit extends GlobalTeleport {

        /**
         * @param robit        The robit that is teleporting home.
         */
        public <ROBIT extends Entity & IRobit> Robit(ROBIT robit) {
            this(robit, Objects.requireNonNull(robit.getHome(), "Robit teleport event cannot be fired for invalid Robits"));
        }

        private <ROBIT extends Entity & IRobit> Robit(ROBIT robit, GlobalPos homeLocation) {
            super(robit, homeLocation.pos().getX() + 0.5, homeLocation.pos().getY() + 0.3, homeLocation.pos().getZ() + 0.5, homeLocation.dimension());
        }
    }

    /**
     * This event is fired before a teleporter or portable teleporter is used.
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
     * <br>
     *
     * @apiNote This event is only fired once for the base entity, and is not fired for any of the passengers that are teleported with it. If you care about seeing
     * passengers (which may be players) you need to check the entity's {@link Entity#getPassengers() passengers}.
     * @since 10.5.2
     */
    public static class Teleporter extends GlobalTeleport {

        private final long energyCost;

        /**
         * @param entity          The entity that is teleporting.
         * @param teleporterPos   Destination teleporter position.
         * @param targetDimension Destination dimension.
         * @param energyCost      The energy cost to perform the teleportation.
         */
        public Teleporter(Entity entity, BlockPos teleporterPos, ResourceKey<Level> targetDimension, long energyCost) {
            super(entity, teleporterPos.getX() + 0.5, teleporterPos.getY(), teleporterPos.getZ() + 0.5, targetDimension);
            this.energyCost = energyCost;
        }

        /**
         * @return The amount of energy the teleportation will cost
         */
        public long getEnergyCost() {
            return energyCost;
        }
    }

    /**
     * This event is fired before a portable teleporter is used.
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
     *
     * @since 10.5.2
     */
    public static class PortableTeleporter extends Teleporter {

        private final ItemStack portableTeleporter;

        /**
         * @param player             The player that is teleporting.
         * @param teleporterPos      Destination teleporter position.
         * @param targetDimension    Destination dimension.
         * @param portableTeleporter Portable Teleporter used for teleportation.
         * @param energyCost         The energy cost to perform the teleportation.
         */
        public PortableTeleporter(Player player, BlockPos teleporterPos, ResourceKey<Level> targetDimension, ItemStack portableTeleporter, long energyCost) {
            super(player, teleporterPos, targetDimension, energyCost);
            this.portableTeleporter = portableTeleporter;
        }

        @Override
        public Player getEntity() {
            return (Player) super.getEntity();
        }

        /**
         * @return The ItemStack for the Portable Teleporter the player is using to teleport.
         */
        public ItemStack getPortableTeleporter() {
            return portableTeleporter;
        }
    }
}