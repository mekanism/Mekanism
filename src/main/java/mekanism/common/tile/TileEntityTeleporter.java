package mekanism.common.tile;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.event.MekanismTeleportEvent;
import mekanism.api.math.ULong;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.network.to_client.PacketSetDeltaMovement;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityTeleporter extends TileEntityMekanism implements IChunkLoader {

    private static final TeleportInfo NO_FRAME = new TeleportInfo((byte) 2, null, Collections.emptyList());
    private static final TeleportInfo NO_LINK = new TeleportInfo((byte) 3, null, Collections.emptyList());
    private static final TeleportInfo NOT_ENOUGH_ENERGY = new TeleportInfo((byte) 4, null, Collections.emptyList());

    public final Set<UUID> didTeleport = new ObjectOpenHashSet<>();
    private final Predicate<Entity> SAME_DIMENSION_TARGET = entity -> canTeleportEntity(entity, null);
    private AABB teleportBounds;
    public int teleDelay = 0;
    public boolean shouldRender;
    @Nullable
    private Direction frameDirection;
    private boolean frameRotated;
    private EnumColor color;

    /**
     * This teleporter's current status.
     */
    public byte status = 0;

    private final TileComponentChunkLoader<TileEntityTeleporter> chunkLoaderComponent;

    private MachineEnergyContainer<TileEntityTeleporter> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityTeleporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.TELEPORTER, pos, state);
        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.TELEPORTER, true, true, false);
        cacheCoord();
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 153, 7));
        return builder.build();
    }

    private boolean canTeleportEntity(Entity entity, @Nullable Level destinationLevel) {
        if (entity.isSpectator() || entity.isPassenger() || entity instanceof PartEntity || entity.getType().is(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED)) {
            return false;
        } else if (destinationLevel != null && !entity.canChangeDimensions(entity.level(), destinationLevel)) {
            return false;
        }
        return !didTeleport.contains(entity.getUUID());
    }

    public static void alignPlayer(ServerPlayer player, MekanismTeleportEvent.Teleporter event, TileEntityTeleporter teleporter) {
        alignPlayer(player, BlockPos.containing(event.getTarget()), teleporter);
    }

    private static void alignPlayer(ServerPlayer player, BlockPos target, TileEntityTeleporter teleporter) {
        Direction side = null;
        if (teleporter.frameDirection != null && teleporter.frameDirection.getAxis().isHorizontal()) {
            //If the frame is horizontal always face towards the other portion of the frame
            side = teleporter.frameDirection;
        } else {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction iterSide : EnumUtils.HORIZONTAL_DIRECTIONS) {
                mutable.setWithOffset(target, iterSide);
                if (player.level().isEmptyBlock(mutable)) {
                    side = iterSide;
                    break;
                }
            }
        }
        float yaw = player.getYRot();
        if (side != null) {
            switch (side) {
                case NORTH -> yaw = 180;
                case SOUTH -> yaw = 0;
                case WEST -> yaw = 90;
                case EAST -> yaw = 270;
            }
        }
        player.connection.teleport(player.getX(), player.getY(), player.getZ(), yaw, player.getXRot());
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        if (teleportBounds == null && frameDirection != null) {
            resetBounds();
        }

        TeleporterFrequency freq = getFrequency(FrequencyType.TELEPORTER);
        TeleportInfo teleportInfo = canTeleport(freq);
        status = teleportInfo.status();
        if (status == 1 && teleDelay == 0 && canFunction()) {
            teleport(freq, teleportInfo);
        }
        if (teleDelay == 0 && teleportBounds != null && !didTeleport.isEmpty()) {
            cleanTeleportCache();
        }

        boolean prevShouldRender = shouldRender;
        shouldRender = status == 1 || status > 4;
        EnumColor prevColor = color;
        color = freq == null ? null : freq.getColor();
        if (shouldRender != prevShouldRender) {
            //This also means the comparator output changed so notify the neighbors we have a change
            WorldUtils.notifyLoadedNeighborsOfTileChange(level, getBlockPos());
            sendUpdatePacket = true;
        } else if (color != prevColor) {
            sendUpdatePacket = true;
        }
        teleDelay = Math.max(0, teleDelay - 1);
        energySlot.fillContainerOrConvert();
        return sendUpdatePacket;
    }

    @Nullable
    private GlobalPos getClosest(@Nullable TeleporterFrequency frequency) {
        return frequency == null ? null : frequency.getClosestCoords(getTileGlobalPos());
    }

    private void cleanTeleportCache() {
        List<UUID> inTeleporter = level.getEntitiesOfClass(Entity.class, teleportBounds).stream().map(Entity::getUUID).toList();
        if (inTeleporter.isEmpty()) {
            didTeleport.clear();
        } else {
            //noinspection Java8CollectionRemoveIf - We can't replace it with removeIf as it has a capturing lambda
            for (Iterator<UUID> iterator = didTeleport.iterator(); iterator.hasNext(); ) {
                UUID id = iterator.next();
                if (!inTeleporter.contains(id)) {
                    iterator.remove();
                }
            }
        }
    }

    private void resetBounds() {
        if (frameDirection == null) {
            teleportBounds = null;
        } else {
            teleportBounds = getTeleporterBoundingBox(frameDirection);
        }
    }

    /**
     * Checks whether, or why not, this teleporter can teleport entities.
     *
     * @return A teleport info with 1: yes, 2: no frame, 3: no link found, 4: not enough electricity. If it is one, then closest coords and to teleport will be present
     *
     * @apiNote Only call on server
     */
    private TeleportInfo canTeleport(@Nullable TeleporterFrequency frequency) {
        Direction direction = getFrameDirection();
        if (direction == null) {
            frameDirection = null;
            return NO_FRAME;
        } else if (frameDirection != direction) {
            frameDirection = direction;
            resetBounds();
        }
        GlobalPos closestCoords = getClosest(frequency);
        if (closestCoords == null || level == null) {
            return NO_LINK;
        }
        boolean sameDimension = level.dimension() == closestCoords.dimension();
        Level targetWorld;
        if (sameDimension) {
            targetWorld = level;
        } else {
            MinecraftServer server = level.getServer();
            if (server == null) {//Should not happen
                return NO_LINK;
            }
            targetWorld = server.getLevel(closestCoords.dimension());
            if (targetWorld == null) {//In theory should not happen
                return NO_LINK;
            }
        }
        List<Entity> toTeleport = getToTeleport(sameDimension, targetWorld);
        long sum = 0;
        for (Entity entity : toTeleport) {
            sum += calculateEnergyCost(entity, targetWorld, closestCoords);
        }
        if (energyContainer.extract(sum, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(sum)) {
            return NOT_ENOUGH_ENERGY;
        }
        return new TeleportInfo((byte) 1, closestCoords, toTeleport);
    }

    public BlockPos getTeleporterTargetPos() {
        if (frameDirection == null) {
            return worldPosition.above();
        } else if (frameDirection == Direction.DOWN) {
            return worldPosition.below(2);
        }
        return worldPosition.relative(frameDirection);
    }

    /**
     * @apiNote Only call this from the server
     */
    public void sendTeleportParticles() {
        BlockPos teleporterTargetPos = getTeleporterTargetPos();
        Direction offsetDirection;
        if (frameDirection == null || frameDirection.getAxis().isVertical()) {
            //Down is up as well because we have the bottom pos be the bottom of the two spots
            offsetDirection = Direction.UP;
        } else {
            offsetDirection = frameDirection;
        }
        PacketUtils.sendToAllTracking(new PacketPortalFX(teleporterTargetPos, offsetDirection), level, teleporterTargetPos);
    }

    /**
     * @apiNote Only call this from the server
     */
    private void teleport(TeleporterFrequency frequency, TeleportInfo teleportInfo) {
        if (teleportInfo.closest == null || level == null || teleportInfo.toTeleport.isEmpty()) {
            return;
        }
        MinecraftServer currentServer = level.getServer();
        if (currentServer == null) {
            return;
        }
        boolean sameDimension = level.dimension() == teleportInfo.closest.dimension();
        Level teleWorld = sameDimension ? level : currentServer.getLevel(teleportInfo.closest.dimension());
        BlockPos closestPos = teleportInfo.closest.pos();
        TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, closestPos);
        if (teleporter != null) {
            Set<GlobalPos> activeCoords = frequency.getActiveCoords();
            BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
            for (Entity entity : teleportInfo.toTeleport) {
                markTeleported(teleporter, entity, sameDimension, teleWorld);
                teleporter.teleDelay = 5;
                //Calculate energy cost before teleporting the entity, as after teleporting it
                // the cost will be negligible due to being on top of the destination
                long energyCost = calculateEnergyCost(entity, teleWorld, teleportInfo.closest);

                MekanismTeleportEvent.Teleporter event = new MekanismTeleportEvent.Teleporter(entity, teleporterTargetPos, teleWorld.dimension(), energyCost);
                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    //Skip the entity if the event was cancelled
                    continue;
                }

                double oldX = entity.getX();
                double oldY = entity.getY();
                double oldZ = entity.getZ();
                Entity teleportedEntity = teleportEntityTo(entity, teleWorld, event, true);
                if (teleportedEntity instanceof ServerPlayer player) {
                    alignPlayer(player, event, teleporter);
                    MekanismCriteriaTriggers.TELEPORT.value().trigger(player);
                }
                for (GlobalPos coords : activeCoords) {
                    Level world = level.dimension() == coords.dimension() ? level : currentServer.getLevel(coords.dimension());
                    TileEntityTeleporter tile = WorldUtils.getTileEntity(TileEntityTeleporter.class, world, coords.pos());
                    if (tile != null) {
                        tile.sendTeleportParticles();
                    }
                }
                energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.INTERNAL);
                if (teleportedEntity != null) {
                    if (level != teleportedEntity.level() || teleportedEntity.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                        //If the entity teleported over 5 blocks, play the sound at both the destination and the source
                        level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
                    }
                    teleportedEntity.level().playSound(null, teleportedEntity.getX(), teleportedEntity.getY(), teleportedEntity.getZ(),
                          SoundEvents.ENDERMAN_TELEPORT, teleportedEntity.getSoundSource(), 1.0F, 1.0F);
                }
            }
        }
    }

    private void markTeleported(TileEntityTeleporter teleporter, Entity entity, boolean sameDimension, Level destinationWorld) {
        if (sameDimension || entity.canChangeDimensions(entity.level(), destinationWorld)) {
            //Only mark the entity as teleported if it will teleport, it is in the same dimension or is able to change dimensions
            // This is mainly important for the passengers as we teleport all entities and passengers up to one that can't change dimensions
            teleporter.didTeleport.add(entity.getUUID());
            for (Entity passenger : entity.getPassengers()) {
                markTeleported(teleporter, passenger, sameDimension, destinationWorld);
            }
        }
    }

    @Nullable
    public static Entity teleportEntityTo(Entity entity, Level targetWorld, MekanismTeleportEvent.Teleporter event, boolean persistMovement) {
        Vec3 destination = event.getTarget();
        if (!event.isTransDimensional()) {
            Vec3 deltaMovement = entity.getDeltaMovement();
            entity.teleportTo(destination.x, destination.y, destination.z);
            if (!entity.getPassengers().isEmpty()) {
                //Force re-apply any passengers so that players don't get "stuck" outside what they may be riding
                ((ServerChunkCache) entity.level().getChunkSource()).broadcast(entity, new ClientboundSetPassengersPacket(entity));
                Entity controller = entity.getControllingPassenger();
                if (controller != entity && controller instanceof ServerPlayer player && !(controller instanceof FakePlayer)) {
                    if (player.connection != null) {
                        //Force sync the fact that the vehicle moved to the client that is controlling it
                        // so that it makes sure to use the correct positions when sending move packets
                        // back to the server instead of running into moved wrongly issues
                        player.connection.send(new ClientboundMoveVehiclePacket(entity));
                    }
                }
            }
            if (persistMovement && entity instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
                player.setDeltaMovement(deltaMovement);
                //Force sync the delta movement to the client so that they don't stop moving due to the teleport and movement being client sided
                PacketDistributor.sendToPlayer(player, new PacketSetDeltaMovement(deltaMovement));
            }
            return entity;
        }
        //Note: We grab the passengers here instead of in placeEntity as changeDimension starts by removing any passengers
        List<Entity> passengers = entity.getPassengers();
        Level fromWorld = entity.level();
        ServerLevel serverLevel = (ServerLevel) targetWorld;
        return entity.changeDimension(new DimensionTransition(serverLevel, destination, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), target -> {
            //Teleport all passengers to the other dimension and then make them start riding the entity again
            for (Entity passenger : passengers) {
                teleportPassenger(fromWorld, serverLevel, destination, target, passenger);
            }
        }));
    }

    private static void teleportPassenger(Level fromWorld, ServerLevel destWorld, Vec3 destination, Entity repositionedEntity, Entity passenger) {
        if (!passenger.canChangeDimensions(fromWorld, destWorld)) {
            //If the passenger can't change dimensions just let it peacefully stay after dismounting rather than trying to teleport it
            return;
        }
        //Note: We grab the passengers here instead of in placeEntity as changeDimension starts by removing any passengers
        List<Entity> passengers = passenger.getPassengers();
        passenger.changeDimension(new DimensionTransition(destWorld, destination, passenger.getDeltaMovement(), passenger.getYRot(), passenger.getXRot(), target -> {
            //TODO - 1.21: Test if this logic for making the entity not take damage works or if the post transition thing, is well past teleport and past damage
            boolean invulnerable = target.isInvulnerable();
            //Make the entity invulnerable so that when we teleport it, it doesn't take damage
            // we revert this state to the previous state after teleporting
            target.setInvulnerable(true);
            //Force our passenger to start riding the new entity again
            target.startRiding(repositionedEntity, true);
            //Teleport "nested" passengers
            for (Entity p : passengers) {
                teleportPassenger(fromWorld, destWorld, destination, target, p);
            }
            target.setInvulnerable(invulnerable);
        }));
    }

    private List<Entity> getToTeleport(boolean sameDimension, Level destinationLevel) {
        //Don't get entities that are currently spectator, are a passenger, are part entities (as the parent entity should be what we teleport),
        // entities that cannot change dimensions if we are teleporting to another dimension, or entities that recently teleported
        //Note: Passengers get handled separately
        if (level == null || teleportBounds == null) {
            return Collections.emptyList();
        }
        return level.getEntitiesOfClass(Entity.class, teleportBounds, sameDimension ? SAME_DIMENSION_TARGET : entity -> canTeleportEntity(entity, destinationLevel));
    }

    /**
     * @apiNote Only call from the server side
     */
    @Nullable
    public static long calculateEnergyCost(Entity entity, GlobalPos pos) {
        MinecraftServer currentServer = entity.getServer();
        if (currentServer != null) {
            Level targetWorld = currentServer.getLevel(pos.dimension());
            if (targetWorld != null) {
                return calculateEnergyCost(entity, targetWorld, pos);
            }
        }
        return -1;
    }

    @NotNull
    public static long calculateEnergyCost(Entity entity, Level targetWorld, GlobalPos coords) {
        long energyCost = MekanismConfig.usage.teleporterBase.get();
        boolean sameDimension = entity.level().dimension() == coords.dimension();
        BlockPos pos = coords.pos();
        if (sameDimension) {
            energyCost = (long) ((energyCost + MekanismConfig.usage.teleporterDistance.get()) * Math.ceil(Math.sqrt(entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()))));
        } else {
            double currentScale = entity.level().dimensionType().coordinateScale();
            double targetScale = targetWorld.dimensionType().coordinateScale();
            double yDifference = entity.getY() - pos.getY();
            //Note: coordinate scale only affects x and z, y is 1:1
            double xDifference, zDifference;
            if (currentScale <= targetScale) {
                //If our current scale is less than or equal our target scale, then the cheapest way of teleporting is to act like we:
                // - changed dimensions
                // - teleported the distance
                double scale = currentScale / targetScale;
                xDifference = entity.getX() * scale - pos.getX();
                zDifference = entity.getZ() * scale - pos.getZ();
            } else {
                //If however our current scale is greater than our target scale, then the cheapest way of teleporting is to act like we:
                // - teleported the distance
                // - changed dimensions
                double inverseScale = targetScale / currentScale;
                xDifference = entity.getX() - pos.getX() * inverseScale;
                zDifference = entity.getZ() - pos.getZ() * inverseScale;
            }
            double distance = Mth.length(xDifference, yDifference, zDifference);
            energyCost = (energyCost + MekanismConfig.usage.teleporterDimensionPenalty.get())
                         + (MekanismConfig.usage.teleporterDistance.get() * (long) Math.ceil(distance));
        }
        //Factor the number of passengers of this entity into the teleportation energy cost
        Set<Entity> passengers = new HashSet<>();
        fillIndirectPassengers(entity, sameDimension, targetWorld, passengers);
        int passengerCount = passengers.size();
        return passengerCount > 0 ? Math.multiplyExact(energyCost, passengerCount) : energyCost;
    }

    private static void fillIndirectPassengers(Entity base, boolean sameDimension, Level targetDimension, Set<Entity> passengers) {
        for (Entity entity : base.getPassengers()) {
            if (sameDimension || entity.canChangeDimensions(entity.level(), targetDimension)) {
                passengers.add(entity);
                fillIndirectPassengers(entity, sameDimension, targetDimension, passengers);
            }
        }
    }

    /**
     * Checks in what direction there is a frame.
     *
     * @return in what direction there is a frame, null if none.
     */
    @Nullable
    public Direction getFrameDirection() {
        //Cache the chunks we are looking up to check the frames of
        // Note: We can use an array based map, because we check suck a small area, that if we do go across chunks
        // we will be in at most two in general due to the size of our teleporter. But given we need to check multiple
        // directions we might end up checking two different cross chunk directions which would end up at three
        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectArrayMap<>(3);
        Object2BooleanMap<BlockPos> cachedIsFrame = new Object2BooleanOpenHashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Direction direction : EnumUtils.DIRECTIONS) {
            if (hasFrame(chunkMap, pos, cachedIsFrame, direction, false)) {
                frameRotated = false;
                return direction;
            } else if (hasFrame(chunkMap, pos, cachedIsFrame, direction, true)) {
                frameRotated = true;
                return direction;
            }
        }
        return null;
    }

    /**
     * Checks whether this Teleporter has a Frame in the given Direction.
     *
     * @param direction the direction from the Teleporter block in which the frame should be.
     * @param rotated   whether the frame is rotated by 90 degrees.
     *
     * @return whether the frame exists.
     */
    private boolean hasFrame(Long2ObjectMap<ChunkAccess> chunkMap, BlockPos.MutableBlockPos pos, Object2BooleanMap<BlockPos> cachedIsFrame, Direction direction,
          boolean rotated) {
        int alternatingX = 0;
        int alternatingY = 0;
        int alternatingZ = 0;
        if (rotated) {
            if (direction.getAxis() == Axis.Z) {
                alternatingX = 1;
            } else {
                alternatingZ = 1;
            }
        } else if (direction.getAxis() == Axis.Y) {
            alternatingX = 1;
        } else {
            alternatingY = 1;
        }
        int xComponent = direction.getStepX();
        int yComponent = direction.getStepY();
        int zComponent = direction.getStepZ();

        //Start by checking the two spots right next to the teleporter, and then checking the opposite corner, as those are the most likely to overlap
        return isFramePair(chunkMap, pos, cachedIsFrame, 0, alternatingX, 0, alternatingY, 0, alternatingZ) &&
               isFrame(chunkMap, pos, cachedIsFrame, 3 * xComponent, 3 * yComponent, 3 * zComponent) &&
               isFramePair(chunkMap, pos, cachedIsFrame, xComponent, alternatingX, yComponent, alternatingY, zComponent, alternatingZ) &&
               isFramePair(chunkMap, pos, cachedIsFrame, 2 * xComponent, alternatingX, 2 * yComponent, alternatingY, 2 * zComponent, alternatingZ) &&
               isFramePair(chunkMap, pos, cachedIsFrame, 3 * xComponent, alternatingX, 3 * yComponent, alternatingY, 3 * zComponent, alternatingZ);
    }

    private boolean isFramePair(Long2ObjectMap<ChunkAccess> chunkMap, BlockPos.MutableBlockPos pos, Object2BooleanMap<BlockPos> cachedIsFrame, int xOffset,
          int alternatingX, int yOffset, int alternatingY, int zOffset, int alternatingZ) {
        return isFrame(chunkMap, pos, cachedIsFrame, xOffset - alternatingX, yOffset - alternatingY, zOffset - alternatingZ) &&
               isFrame(chunkMap, pos, cachedIsFrame, xOffset + alternatingX, yOffset + alternatingY, zOffset + alternatingZ);
    }

    private boolean isFrame(Long2ObjectMap<ChunkAccess> chunkMap, BlockPos.MutableBlockPos pos, Object2BooleanMap<BlockPos> cachedIsFrame, int xOffset, int yOffset, int zOffset) {
        pos.setWithOffset(worldPosition, xOffset, yOffset, zOffset);
        if (cachedIsFrame.containsKey(pos)) {
            return cachedIsFrame.getBoolean(pos);
        }
        boolean isFrame = WorldUtils.getBlockState(level, chunkMap, pos)
              .filter(blockState -> blockState.is(MekanismBlocks.TELEPORTER_FRAME.getBlock()))
              .isPresent();
        cachedIsFrame.put(pos.immutable(), isFrame);
        return isFrame;
    }

    /**
     * Gets the direction from the teleporter in which the frame is.
     *
     * @return the direction of the frame.
     */
    @Nullable
    public Direction frameDirection() {
        if (frameDirection == null) {
            return getFrameDirection();
        }
        return frameDirection;
    }

    /**
     * Gets whether the frame is rotated by 90 degrees around the direction axis.
     *
     * @return whether the frame is rotated by 90 degrees.
     */
    public boolean frameRotated() {
        return frameRotated;
    }

    public AABB getTeleporterBoundingBox(@NotNull Direction frameDirection) {
        //Note: We only include the area inside the frame, we don't bother including the teleporter's block itself
        return AABB.encapsulatingFullBlocks(worldPosition.relative(frameDirection), worldPosition.relative(frameDirection, 2));
    }

    @Override
    public TileComponentChunkLoader<TileEntityTeleporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(new ChunkPos(getBlockPos()));
    }

    @Override
    public int getRedstoneLevel() {
        return shouldRender ? 15 : 0;
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return false;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the teleporter
        return getRedstoneLevel();
    }

    public MachineEnergyContainer<TileEntityTeleporter> getEnergyContainer() {
        return energyContainer;
    }

    public EnumColor getColor() {
        return color;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableByte.create(() -> status, value -> status = value));
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putBoolean(SerializationConstants.RENDERING, shouldRender);
        if (color != null) {
            NBTUtils.writeEnum(updateTag, SerializationConstants.COLOR, color);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setBooleanIfPresent(tag, SerializationConstants.RENDERING, value -> shouldRender = value);
        color = NBTUtils.getEnum(tag, SerializationConstants.COLOR, EnumColor.BY_ID);
    }

    //Methods relating to IComputerTile
    @ComputerMethod(methodDescription = "Lists public frequencies")
    Collection<TeleporterFrequency> getFrequencies() {
        return FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod
    boolean hasFrequency() {
        TeleporterFrequency frequency = getFrequency(FrequencyType.TELEPORTER);
        return frequency != null && frequency.isValid() && !frequency.isRemoved();
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    TeleporterFrequency getFrequency() throws ComputerException {
        TeleporterFrequency frequency = getFrequency(FrequencyType.TELEPORTER);
        if (frequency == null || !frequency.isValid() || frequency.isRemoved()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a public frequency to exist")
    void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public teleporter frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyType.TELEPORTER, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires frequency to not already exist and for it to be public so that it can make it as the player who owns the block. Also sets the frequency after creation")
    void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public teleporter frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyType.TELEPORTER, new FrequencyIdentity(name, SecurityMode.PUBLIC, getOwnerUUID()), getOwnerUUID());
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    EnumColor getFrequencyColor() throws ComputerException {
        return getFrequency().getColor();
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void setFrequencyColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        getFrequency().setColor(color);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void incrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = getFrequency();
        frequency.setColor(frequency.getColor().getNext());
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Requires a frequency to be selected")
    void decrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = getFrequency();
        frequency.setColor(frequency.getColor().getPrevious());
    }

    @ComputerMethod(methodDescription = "Requires a frequency to be selected")
    Set<GlobalPos> getActiveTeleporters() throws ComputerException {
        return getFrequency().getActiveCoords();
    }

    @ComputerMethod
    String getStatus() {
        if (hasFrequency()) {
            return switch (status) {
                case 1 -> "ready";
                case 2 -> "no frame";
                case 4 -> "needs energy";
                default -> "no link";
            };
        }
        return "no frequency";
    }
    //End methods IComputerTile

    private record TeleportInfo(byte status, @Nullable GlobalPos closest, List<Entity> toTeleport) {
    }
}
