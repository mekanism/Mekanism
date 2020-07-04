package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.block.basic.BlockTeleporterFrame;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class TileEntityTeleporter extends TileEntityMekanism implements IChunkLoader {

    public final Set<UUID> didTeleport = new ObjectOpenHashSet<>();
    private AxisAlignedBB teleportBounds = null;
    public int teleDelay = 0;
    public boolean shouldRender;
    private Direction frameDirection = null;
    private boolean frameRotated = false;

    /**
     * This teleporter's current status.
     */
    public byte status = 0;

    private final TileComponentChunkLoader<TileEntityTeleporter> chunkLoaderComponent;

    private MachineEnergyContainer<TileEntityTeleporter> energyContainer;
    private EnergyInventorySlot energySlot;

    public TileEntityTeleporter() {
        super(MekanismBlocks.TELEPORTER);
        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
        frequencyComponent.track(FrequencyType.TELEPORTER, true, true, false);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 153, 7));
        return builder.build();
    }

    public static void alignPlayer(ServerPlayerEntity player, BlockPos position) {
        Direction side = null;
        float yaw = player.rotationYaw;
        BlockPos upperPos = position.up();
        for (Direction iterSide : MekanismUtils.SIDE_DIRS) {
            if (player.world.isAirBlock(upperPos.offset(iterSide))) {
                side = iterSide;
                break;
            }
        }

        if (side != null) {
            switch (side) {
                case NORTH:
                    yaw = 180;
                    break;
                case SOUTH:
                    yaw = 0;
                    break;
                case WEST:
                    yaw = 90;
                    break;
                case EAST:
                    yaw = 270;
                    break;
                default:
                    break;
            }
        }
        player.connection.setPlayerLocation(player.getPosX(), player.getPosY(), player.getPosZ(), yaw, player.rotationPitch);
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
        if (teleDelay == 0 && !didTeleport.isEmpty()) {
            cleanTeleportCache();
        }

        boolean prevShouldRender = shouldRender;
        shouldRender = status == 1 || status > 4;
        if (shouldRender != prevShouldRender) {
            //This also means the comparator output changed so notify the neighbors we have a change
            MekanismUtils.notifyLoadedNeighborsOfTileChange(world, getPos());
            sendUpdatePacket();
        }
        teleDelay = Math.max(0, teleDelay - 1);
        energySlot.fillContainerOrConvert();
    }

    @Nullable
    private Coord4D getClosest() {
        return getFrequency(FrequencyType.TELEPORTER) == null ? null : getFrequency(FrequencyType.TELEPORTER).getClosestCoords(Coord4D.get(this));
    }

    private void cleanTeleportCache() {
        List<UUID> list = new ArrayList<>();
        for (Entity e : world.getEntitiesWithinAABB(Entity.class, teleportBounds)) {
            list.add(e.getUniqueID());
        }
        Set<UUID> teleportCopy = new ObjectOpenHashSet<>(didTeleport);
        for (UUID id : teleportCopy) {
            if (!list.contains(id)) {
                didTeleport.remove(id);
            }
        }
    }

    private void resetBounds() {
        switch(frameDirection) {
            case UP:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
                break;
            case DOWN:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, -3, 1));
                break;
            case EAST:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(3, 1, 1));
                break;
            case WEST:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(-3, 1, 1));
                break;
            case NORTH:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 1, -3));
                break;
            case SOUTH:
                teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 1, 3));
                break;
            default:
                teleportBounds = null;
                break;
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
        for (Entity entity : getToTeleport()) {
            sum = sum.plusEqual(calculateEnergyCost(entity, closestCoords));
        }
        if (energyContainer.getEnergy().smallerThan(sum)) {
            return 4;
        }
        return 1;
    }

    /**
     * @apiNote Only call this from the server
     */
    private void teleport() {
        Coord4D closestCoords = getClosest();
        if (closestCoords == null) {
            return;
        }
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        World teleWorld = currentServer.getWorld(closestCoords.dimension);
        BlockPos closestPos = closestCoords.getPos();
        TileEntityTeleporter teleporter = MekanismUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, closestPos);
        if (teleporter != null) {
            for (Entity entity : getToTeleport()) {
                entity.getSelfAndPassengers().forEach(e -> teleporter.didTeleport.add(e.getUniqueID()));
                teleporter.teleDelay = 5;
                teleportEntityTo(entity, closestCoords, teleporter);
                if (entity instanceof ServerPlayerEntity) {
                    alignPlayer((ServerPlayerEntity) entity, closestPos);
                }
                for (Coord4D coords : getFrequency(FrequencyType.TELEPORTER).getActiveCoords()) {
                    TileEntity tileEntity = MekanismUtils.getTileEntity(world, coords.getPos());
                    if (!(tileEntity instanceof TileEntityTeleporter)) {
                        continue;
                    }
                    BlockPos coordsPos = coords.getPos().down();
                    TileEntityTeleporter porter = (TileEntityTeleporter) tileEntity;
                    if(porter.frameDirection == null) {
                        coordsPos = coordsPos.up();
                    } else {
                        coordsPos = coordsPos.offset(porter.frameDirection);
                    }
                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coordsPos), currentServer.getWorld(coords.dimension), coordsPos);
                }
                energyContainer.extract(calculateEnergyCost(entity, closestCoords), Action.EXECUTE, AutomationType.INTERNAL);
                world.playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, entity.getSoundCategory(), 1.0F, 1.0F, false);
            }
        }
    }

    public static void teleportEntityTo(Entity entity, Coord4D coord, TileEntityTeleporter teleporter) {
        BlockPos target = coord.getPos();
        if (teleporter.frameDirection == null) {
            target = target.up();
        } else if (teleporter.frameDirection == Direction.DOWN) {
            target = target.add(0.5, -2, 0.5);
        } else {
            target = target.offset(teleporter.frameDirection);
        }
        if (entity.world.func_234923_W_() == coord.dimension) {
            entity.setPositionAndUpdate(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        } else {
            ServerWorld newWorld = ((ServerWorld) teleporter.getWorld()).getServer().getWorld(coord.dimension);
            Entity newEntity = entity.func_241206_a_(newWorld);
            newEntity.setPositionAndUpdate(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            //TODO - 1.16: Use this method of teleporting the entity instead once https://github.com/MinecraftForge/MinecraftForge/pull/6886 is merged
            /*entity.changeDimension(newWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity repositionedEntity = repositionEntity.apply(false);
                    repositionedEntity.setPositionAndUpdate(coord.getX() + 0.5, coord.getY() + 1, coord.getZ() + 0.5);
                    return repositionedEntity;
                }
            });*/
        }
    }

    private List<Entity> getToTeleport() {
        return world == null ? Collections.emptyList() : world.getEntitiesWithinAABB(Entity.class, teleportBounds,
              entity -> !entity.isSpectator() && !didTeleport.contains(entity.getUniqueID()));
    }

    @Nonnull
    public static FloatingLong calculateEnergyCost(Entity entity, Coord4D coords) {
        FloatingLong energyCost = MekanismConfig.usage.teleporterBase.get();
        if (entity.world.func_230315_m_().equals(coords.dimension)) {
            energyCost = energyCost.add(MekanismConfig.usage.teleporterDistance.get().multiply(Math.sqrt(entity.getDistanceSq(coords.getX(), coords.getY(), coords.getZ()))));
        } else {
            energyCost = energyCost.add(MekanismConfig.usage.teleporterDimensionPenalty.get());
        }
        return energyCost;
    }

    /**
     * Checks in what direction there is a frame.
     * 
     * @return in what direction there is a frame, null if none.
     */
    @Nullable
    private Direction getFrameDirection() {
        for (Direction direction : Direction.values()) {
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
     * @param rotated whether the frame is rotated by 90 degrees.
     * @return whether the frame exists.
     */
    public boolean hasFrame(Direction direction, boolean rotated) {
        int x = getPos().getX();
        int y = getPos().getY();
        int z = getPos().getZ();
        switch(direction) {
            case UP:
                if (rotated) {
                    return isFrame(x, y, z - 1) && isFrame(x, y, z + 1) && isFrame(x, y + 1, z - 1) && isFrame(x, y + 1, z + 1)
                          && isFrame(x, y + 2, z - 1) && isFrame(x, y + 2, z + 1) && isFrame(x, y + 3, z - 1) && isFrame(x, y + 3, z + 1)
                          && isFrame(x, y + 3, z);
                } else {
                    return isFrame(x - 1, y, z) && isFrame(x + 1, y, z) && isFrame(x - 1, y + 1, z) && isFrame(x + 1, y + 1, z)
                          && isFrame(x - 1, y + 2, z) && isFrame(x + 1, y + 2, z) && isFrame(x - 1, y + 3, z) && isFrame(x + 1, y + 3, z)
                          && isFrame(x, y + 3, z);
                }

            case DOWN:
                if (rotated) {
                    return isFrame(x, y, z - 1) && isFrame(x, y, z + 1) && isFrame(x, y - 1, z - 1) && isFrame(x, y - 1, z + 1)
                          && isFrame(x, y - 2, z - 1) && isFrame(x, y - 2, z + 1) && isFrame(x, y - 3, z - 1) && isFrame(x, y - 3, z + 1)
                          && isFrame(x, y - 3, z);
                } else {
                    return isFrame(x - 1, y, z) && isFrame(x + 1, y, z) && isFrame(x - 1, y - 1, z) && isFrame(x + 1, y - 1, z)
                          && isFrame(x - 1, y - 2, z) && isFrame(x + 1, y - 2, z) && isFrame(x - 1, y - 3, z) && isFrame(x + 1, y - 3, z)
                          && isFrame(x, y - 3, z);
                }

            case EAST:
                if (rotated) {
                    return isFrame(x, y, z - 1) && isFrame(x, y, z + 1) && isFrame(x + 1, y, z - 1) && isFrame(x + 1, y, z + 1)
                          && isFrame(x + 2, y, z - 1) && isFrame(x + 2, y, z + 1) && isFrame(x + 3, y, z - 1) && isFrame(x + 3, y, z + 1)
                          && isFrame(x + 3, y, z);
                } else {
                    return isFrame(x, y - 1, z) && isFrame(x, y + 1, z) && isFrame(x + 1, y - 1, z) && isFrame(x + 1, y + 1, z)
                          && isFrame(x + 2, y - 1, z) && isFrame(x + 2, y + 1, z) && isFrame(x + 3, y - 1, z) && isFrame(x + 3, y + 1, z)
                          && isFrame(x + 3, y, z);
                }

            case WEST:
                if (rotated) {
                    return isFrame(x, y, z - 1) && isFrame(x, y, z + 1) && isFrame(x - 1, y, z - 1) && isFrame(x - 1, y, z + 1)
                          && isFrame(x - 2, y, z - 1) && isFrame(x - 2, y, z + 1) && isFrame(x - 3, y, z - 1) && isFrame(x - 3, y, z + 1)
                          && isFrame(x - 3, y, z);
                } else {
                    return isFrame(x, y - 1, z) && isFrame(x, y + 1, z) && isFrame(x - 1, y - 1, z) && isFrame(x - 1, y + 1, z)
                          && isFrame(x - 2, y - 1, z) && isFrame(x - 2, y + 1, z) && isFrame(x - 3, y - 1, z) && isFrame(x - 3, y + 1, z)
                          && isFrame(x - 3, y, z);
                }

            case NORTH:
                if (rotated) {
                    return isFrame(x - 1, y, z) && isFrame(x + 1, y, z) && isFrame(x - 1, y, z - 1) && isFrame(x + 1, y, z - 1)
                          && isFrame(x - 1, y, z - 2) && isFrame(x + 1, y, z - 2) && isFrame(x - 1, y, z - 3) && isFrame(x + 1, y, z - 3)
                          && isFrame(x, y, z - 3);
                } else {
                    return isFrame(x, y - 1, z) && isFrame(x, y + 1, z) && isFrame(x, y - 1, z - 1) && isFrame(x, y + 1, z - 1)
                          && isFrame(x, y - 1, z - 2) && isFrame(x, y + 1, z - 2) && isFrame(x, y - 1, z - 3) && isFrame(x, y + 1, z - 3)
                          && isFrame(x, y, z - 3);
                }

            case SOUTH:
                if (rotated) {
                    return isFrame(x - 1, y, z) && isFrame(x + 1, y, z) && isFrame(x - 1, y, z + 1) && isFrame(x + 1, y, z + 1)
                          && isFrame(x - 1, y, z + 2) && isFrame(x + 1, y, z + 2) && isFrame(x - 1, y, z + 3) && isFrame(x + 1, y, z + 3)
                          && isFrame(x, y, z + 3);
                } else {
                    return isFrame(x, y - 1, z) && isFrame(x, y + 1, z) && isFrame(x, y - 1, z + 1) && isFrame(x, y + 1, z + 1)
                          && isFrame(x, y - 1, z + 2) && isFrame(x, y + 1, z + 2) && isFrame(x, y - 1, z + 3) && isFrame(x, y + 1, z + 3)
                          && isFrame(x, y, z + 3);
                }

            default:
                return false;
        }
    }

    public boolean isFrame(int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof BlockTeleporterFrame;
    }

    /**
     * Gets the direction from the teleporter in which the frame is.
     * 
     * @return the direction of the frame.
     */
    public Direction frameDirection() {
        if(frameDirection == null) {
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
    public AxisAlignedBB getRenderBoundingBox() {
        //The render bounding box goes where the portal is, we can ignore rendering it
        // if the only thing that is in view is the teleporter itself
        return new AxisAlignedBB(pos.up(), pos.add(1, 3, 1));
    }

    @Override
    public TileComponentChunkLoader<TileEntityTeleporter> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        return Collections.singleton(new ChunkPos(getPos()));
    }

    @Override
    public int getRedstoneLevel() {
        return shouldRender ? 15 : 0;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the teleporter
        return getRedstoneLevel();
    }

    public MachineEnergyContainer<TileEntityTeleporter> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableByte.create(() -> status, value -> status = value));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, shouldRender);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> shouldRender = value);
    }
}