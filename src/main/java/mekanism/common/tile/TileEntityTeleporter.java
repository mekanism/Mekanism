package mekanism.common.tile;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
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
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TileEntityTeleporter extends TileEntityMekanism implements IChunkLoader {

    public final Set<UUID> didTeleport = new ObjectOpenHashSet<>();
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
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityTeleporter(BlockPos pos, BlockState state) {
        super(MekanismBlocks.TELEPORTER, pos, state);
        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.TELEPORTER, true, true, false);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
        cacheCoord();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 153, 7));
        return builder.build();
    }

    public static void alignPlayer(ServerPlayer player, BlockPos target, TileEntityTeleporter teleporter) {
        Direction side = null;
        if (teleporter.frameDirection != null && teleporter.frameDirection.getAxis().isHorizontal()) {
            //If the frame is horizontal always face towards the other portion of the frame
            side = teleporter.frameDirection;
        } else {
            for (Direction iterSide : EnumUtils.HORIZONTAL_DIRECTIONS) {
                if (player.level.isEmptyBlock(target.relative(iterSide))) {
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
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (teleportBounds == null && frameDirection != null) {
            resetBounds();
        }

        status = canTeleport();
        if (MekanismUtils.canFunction(this) && status == 1 && teleDelay == 0) {
            teleport();
        }
        if (teleDelay == 0 && teleportBounds != null && !didTeleport.isEmpty()) {
            cleanTeleportCache();
        }

        boolean prevShouldRender = shouldRender;
        shouldRender = status == 1 || status > 4;
        EnumColor prevColor = color;
        TeleporterFrequency freq = getFrequency(FrequencyType.TELEPORTER);
        color = freq == null ? null : freq.getColor();
        if (shouldRender != prevShouldRender) {
            //This also means the comparator output changed so notify the neighbors we have a change
            WorldUtils.notifyLoadedNeighborsOfTileChange(level, getBlockPos());
            sendUpdatePacket();
        } else if (color != prevColor) {
            sendUpdatePacket();
        }
        teleDelay = Math.max(0, teleDelay - 1);
        energySlot.fillContainerOrConvert();
    }

    @Nullable
    private Coord4D getClosest() {
        TeleporterFrequency frequency = getFrequency(FrequencyType.TELEPORTER);
        return frequency == null ? null : frequency.getClosestCoords(getTileCoord());
    }

    private void cleanTeleportCache() {
        List<UUID> inTeleporter = level.getEntitiesOfClass(Entity.class, teleportBounds).stream().map(Entity::getUUID).toList();
        if (inTeleporter.isEmpty()) {
            didTeleport.clear();
        } else {
            didTeleport.removeIf(id -> !inTeleporter.contains(id));
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
     * @return 1: yes, 2: no frame, 3: no link found, 4: not enough electricity
     */
    private byte canTeleport() {
        Direction direction = getFrameDirection();
        if (direction == null) {
            frameDirection = null;
            return 2;
        } else if (frameDirection != direction) {
            frameDirection = direction;
            resetBounds();
        }
        Coord4D closestCoords = getClosest();
        if (closestCoords == null) {
            return 3;
        }
        FloatingLong sum = FloatingLong.ZERO;
        for (Entity entity : getToTeleport(level.dimension() == closestCoords.dimension)) {
            sum = sum.plusEqual(calculateEnergyCost(entity, closestCoords));
        }
        if (energyContainer.extract(sum, Action.SIMULATE, AutomationType.INTERNAL).smallerThan(sum)) {
            return 4;
        }
        return 1;
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
        Mekanism.packetHandler().sendToAllTracking(new PacketPortalFX(teleporterTargetPos, offsetDirection), level, teleporterTargetPos);
    }

    /**
     * @apiNote Only call this from the server
     */
    private void teleport() {
        Coord4D closestCoords = getClosest();
        if (closestCoords == null || level == null) {
            return;
        }
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        Level teleWorld = currentServer.getLevel(closestCoords.dimension);
        BlockPos closestPos = closestCoords.getPos();
        TileEntityTeleporter teleporter = WorldUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, closestPos);
        if (teleporter != null) {
            boolean sameDimension = level.dimension() == closestCoords.dimension;
            List<Entity> entitiesToTeleport = getToTeleport(sameDimension);
            if (!entitiesToTeleport.isEmpty()) {
                Set<Coord4D> activeCoords = getFrequency(FrequencyType.TELEPORTER).getActiveCoords();
                BlockPos teleporterTargetPos = teleporter.getTeleporterTargetPos();
                for (Entity entity : entitiesToTeleport) {
                    markTeleported(teleporter, entity, sameDimension);
                    teleporter.teleDelay = 5;
                    //Calculate energy cost before teleporting the entity, as after teleporting it
                    // the cost will be negligible due to being on top of the destination
                    FloatingLong energyCost = calculateEnergyCost(entity, closestCoords);
                    double oldX = entity.getX();
                    double oldY = entity.getY();
                    double oldZ = entity.getZ();
                    Entity teleportedEntity = teleportEntityTo(entity, teleWorld, teleporterTargetPos);
                    if (teleportedEntity instanceof ServerPlayer player) {
                        alignPlayer(player, teleporterTargetPos, teleporter);
                    }
                    for (Coord4D coords : activeCoords) {
                        Level world = level.dimension() == coords.dimension ? level : currentServer.getLevel(coords.dimension);
                        TileEntityTeleporter tile = WorldUtils.getTileEntity(TileEntityTeleporter.class, world, coords.getPos());
                        if (tile != null) {
                            tile.sendTeleportParticles();
                        }
                    }
                    energyContainer.extract(energyCost, Action.EXECUTE, AutomationType.INTERNAL);
                    if (teleportedEntity != null) {
                        if (level != teleportedEntity.level || teleportedEntity.distanceToSqr(oldX, oldY, oldZ) >= 25) {
                            //If the entity teleported over 5 blocks, play the sound at both the destination and the source
                            level.playSound(null, oldX, oldY, oldZ, SoundEvents.ENDERMAN_TELEPORT, entity.getSoundSource(), 1.0F, 1.0F);
                        }
                        teleportedEntity.level.playSound(null, teleportedEntity.getX(), teleportedEntity.getY(), teleportedEntity.getZ(),
                              SoundEvents.ENDERMAN_TELEPORT, teleportedEntity.getSoundSource(), 1.0F, 1.0F);
                    }
                }
            }
        }
    }

    private void markTeleported(TileEntityTeleporter teleporter, Entity entity, boolean sameDimension) {
        if (sameDimension || entity.canChangeDimensions()) {
            //Only mark the entity as teleported if it will teleport, it is in the same dimension or is able to change dimensions
            // This is mainly important for the passengers as we teleport all entities and passengers up to one that can't change dimensions
            teleporter.didTeleport.add(entity.getUUID());
            for (Entity passenger : entity.getPassengers()) {
                markTeleported(teleporter, passenger, sameDimension);
            }
        }
    }

    @Nullable
    public static Entity teleportEntityTo(Entity entity, Level targetWorld, BlockPos target) {
        if (entity.getCommandSenderWorld().dimension() == targetWorld.dimension()) {
            entity.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            if (!entity.getPassengers().isEmpty()) {
                //Force re-apply any passengers so that players don't get "stuck" outside what they may be riding
                ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, new ClientboundSetPassengersPacket(entity));
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
            return entity;
        }
        Vec3 destination = new Vec3(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        //Note: We grab the passengers here instead of in placeEntity as changeDimension starts by removing any passengers
        List<Entity> passengers = entity.getPassengers();
        return entity.changeDimension((ServerLevel) targetWorld, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                Entity repositionedEntity = repositionEntity.apply(false);
                if (repositionedEntity != null) {
                    //Teleport all passengers to the other dimension and then make them start riding the entity again
                    for (Entity passenger : passengers) {
                        teleportPassenger(destWorld, destination, repositionedEntity, passenger);
                    }
                }
                return repositionedEntity;
            }

            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                return new PortalInfo(destination, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }

    private static void teleportPassenger(ServerLevel destWorld, Vec3 destination, Entity repositionedEntity, Entity passenger) {
        if (!passenger.canChangeDimensions()) {
            //If the passenger can't change dimensions just let it peacefully stay after dismounting rather than trying to teleport it
            return;
        }
        //Note: We grab the passengers here instead of in placeEntity as changeDimension starts by removing any passengers
        List<Entity> passengers = passenger.getPassengers();
        passenger.changeDimension(destWorld, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                boolean invulnerable = entity.isInvulnerable();
                //Make the entity invulnerable so that when we teleport it, it doesn't take damage
                // we revert this state to the previous state after teleporting
                entity.setInvulnerable(true);
                Entity repositionedPassenger = repositionEntity.apply(false);
                if (repositionedPassenger != null) {
                    //Force our passenger to start riding the new entity again
                    repositionedPassenger.startRiding(repositionedEntity, true);
                    //Teleport "nested" passengers
                    for (Entity passenger : passengers) {
                        teleportPassenger(destWorld, destination, repositionedPassenger, passenger);
                    }
                    repositionedPassenger.setInvulnerable(invulnerable);
                }
                entity.setInvulnerable(invulnerable);
                return repositionedPassenger;
            }

            @Override
            public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
                //This is needed to ensure the passenger starts getting tracked after teleporting
                return new PortalInfo(destination, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot());
            }

            @Override
            public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
                return false;
            }
        });
    }

    private List<Entity> getToTeleport(boolean sameDimension) {
        //Don't get entities that are currently spectator, are a passenger, are part entities (as the parent entity should be what we teleport),
        // entities that cannot change dimensions if we are teleporting to another dimension, or entities that recently teleported
        //Note: Passengers get handled separately
        return level == null || teleportBounds == null ? Collections.emptyList() : level.getEntitiesOfClass(Entity.class, teleportBounds,
              entity -> !entity.isSpectator() && !entity.isPassenger() && !(entity instanceof PartEntity) &&
                        (sameDimension || entity.canChangeDimensions()) && !didTeleport.contains(entity.getUUID()));
    }

    @Nonnull
    public static FloatingLong calculateEnergyCost(Entity entity, Coord4D coords) {
        FloatingLong energyCost = MekanismConfig.usage.teleporterBase.get();
        if (entity.level.dimension() == coords.dimension) {
            energyCost = energyCost.add(MekanismConfig.usage.teleporterDistance.get().multiply(Math.sqrt(entity.distanceToSqr(coords.getX(), coords.getY(), coords.getZ()))));
        } else {
            energyCost = energyCost.add(MekanismConfig.usage.teleporterDimensionPenalty.get());
        }
        //Factor the number of passengers of this entity into the teleportation energy cost
        Set<Entity> passengers = new HashSet<>();
        fillIndirectPassengers(entity, entity.level.dimension() == coords.dimension, passengers);
        int passengerCount = passengers.size();
        return passengerCount > 0 ? energyCost.multiply(passengerCount) : energyCost;
    }

    private static void fillIndirectPassengers(Entity base, boolean sameDimension, Set<Entity> passengers) {
        for (Entity entity : base.getPassengers()) {
            if (sameDimension || entity.canChangeDimensions()) {
                passengers.add(entity);
                fillIndirectPassengers(entity, sameDimension, passengers);
            }
        }
    }

    /**
     * Checks in what direction there is a frame.
     *
     * @return in what direction there is a frame, null if none.
     */
    @Nullable
    private Direction getFrameDirection() {
        for (Direction direction : EnumUtils.DIRECTIONS) {
            if (hasFrame(direction, false)) {
                frameRotated = false;
                return direction;
            } else if (hasFrame(direction, true)) {
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
    private boolean hasFrame(Direction direction, boolean rotated) {
        int alternatingX = 0;
        int alternatingY = 0;
        int alternatingZ = 0;
        if (rotated) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                alternatingX = 1;
            } else {
                alternatingZ = 1;
            }
        } else if (direction == Direction.UP || direction == Direction.DOWN) {
            alternatingX = 1;
        } else {
            alternatingY = 1;
        }
        int xComponent = direction.getStepX();
        int yComponent = direction.getStepY();
        int zComponent = direction.getStepZ();
        //Cache the chunks we are looking up to check the frames of
        // Note: We can use an array based map, because we check suck a small area, that if we do go across chunks
        // we will be in at most two in general due to the size of our teleporter. But given we need to check multiple
        // directions we might end up checking two different cross chunk directions which would end up at three
        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectArrayMap<>(3);
        return isFramePair(chunkMap, 0, alternatingX, 0, alternatingY, 0, alternatingZ) &&
               isFramePair(chunkMap, xComponent, alternatingX, yComponent, alternatingY, zComponent, alternatingZ) &&
               isFramePair(chunkMap, 2 * xComponent, alternatingX, 2 * yComponent, alternatingY, 2 * zComponent, alternatingZ) &&
               isFramePair(chunkMap, 3 * xComponent, alternatingX, 3 * yComponent, alternatingY, 3 * zComponent, alternatingZ) &&
               isFrame(chunkMap, 3 * xComponent, 3 * yComponent, 3 * zComponent);
    }

    private boolean isFramePair(Long2ObjectMap<ChunkAccess> chunkMap, int xOffset, int alternatingX, int yOffset, int alternatingY, int zOffset, int alternatingZ) {
        return isFrame(chunkMap, xOffset - alternatingX, yOffset - alternatingY, zOffset - alternatingZ) &&
               isFrame(chunkMap, xOffset + alternatingX, yOffset + alternatingY, zOffset + alternatingZ);
    }

    private boolean isFrame(Long2ObjectMap<ChunkAccess> chunkMap, int xOffset, int yOffset, int zOffset) {
        Optional<BlockState> state = WorldUtils.getBlockState(level, chunkMap, worldPosition.offset(xOffset, yOffset, zOffset));
        return state.filter(blockState -> blockState.is(MekanismBlocks.TELEPORTER_FRAME.getBlock())).isPresent();
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

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        //Note: If the frame direction is "null" we instead just only mark the teleporter itself.
        Direction frameDirection = getFrameDirection();
        return frameDirection == null ? new AABB(worldPosition, worldPosition.offset(1, 1, 1)) : getTeleporterBoundingBox(frameDirection);
    }

    private AABB getTeleporterBoundingBox(@Nonnull Direction frameDirection) {
        //Note: We only include the area inside the frame, we don't bother including the teleporter's block itself
        return switch (frameDirection) {
            case UP -> new AABB(worldPosition.above(), worldPosition.offset(1, 3, 1));
            case DOWN -> new AABB(worldPosition, worldPosition.offset(1, -2, 1));
            case EAST -> new AABB(worldPosition.east(), worldPosition.offset(3, 1, 1));
            case WEST -> new AABB(worldPosition, worldPosition.offset(-2, 1, 1));
            case NORTH -> new AABB(worldPosition, worldPosition.offset(1, 1, -2));
            case SOUTH -> new AABB(worldPosition.south(), worldPosition.offset(1, 1, 3));
        };
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
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
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

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, shouldRender);
        if (color != null) {
            NBTUtils.writeEnum(updateTag, NBTConstants.COLOR, color);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> shouldRender = value);
        if (tag.contains(NBTConstants.COLOR, Tag.TAG_INT)) {
            color = EnumColor.byIndexStatic(tag.getInt(NBTConstants.COLOR));
        } else {
            color = null;
        }
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private Collection<TeleporterFrequency> getFrequencies() {
        return FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequencies();
    }

    @ComputerMethod
    private boolean hasFrequency() {
        TeleporterFrequency frequency = getFrequency(FrequencyType.TELEPORTER);
        return frequency != null && frequency.isValid();
    }

    @ComputerMethod
    private TeleporterFrequency getFrequency() throws ComputerException {
        TeleporterFrequency frequency = getFrequency(FrequencyType.TELEPORTER);
        if (frequency == null || !frequency.isValid()) {
            throw new ComputerException("No frequency is currently selected.");
        }
        return frequency;
    }

    @ComputerMethod
    private void setFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency == null) {
            throw new ComputerException("No public teleporter frequency with name '%s' found.", name);
        }
        setFrequency(FrequencyType.TELEPORTER, frequency.getIdentity(), getOwnerUUID());
    }

    @ComputerMethod
    private void createFrequency(String name) throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = FrequencyType.TELEPORTER.getManagerWrapper().getPublicManager().getFrequency(name);
        if (frequency != null) {
            throw new ComputerException("Unable to create public teleporter frequency with name '%s' as one already exists.", name);
        }
        setFrequency(FrequencyType.TELEPORTER, new FrequencyIdentity(name, true), getOwnerUUID());
    }

    @ComputerMethod
    private EnumColor getFrequencyColor() throws ComputerException {
        return getFrequency().getColor();
    }

    @ComputerMethod
    private void setFrequencyColor(EnumColor color) throws ComputerException {
        validateSecurityIsPublic();
        getFrequency().setColor(color);
    }

    @ComputerMethod
    private void incrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = getFrequency();
        frequency.setColor(frequency.getColor().getNext());
    }

    @ComputerMethod
    private void decrementFrequencyColor() throws ComputerException {
        validateSecurityIsPublic();
        TeleporterFrequency frequency = getFrequency();
        frequency.setColor(frequency.getColor().getPrevious());
    }

    @ComputerMethod
    private Set<Coord4D> getActiveTeleporters() throws ComputerException {
        return getFrequency().getActiveCoords();
    }

    @ComputerMethod
    private String getStatus() {
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
}