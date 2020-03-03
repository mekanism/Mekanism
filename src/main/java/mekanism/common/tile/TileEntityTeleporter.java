package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.block.basic.BlockTeleporterFrame;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.chunkloading.IChunkLoader;
import mekanism.common.config.MekanismConfig;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.frequency.IFrequencyHandler;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableByte;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class TileEntityTeleporter extends TileEntityMekanism implements IChunkLoader, IFrequencyHandler {

    public AxisAlignedBB teleportBounds = null;

    public Set<UUID> didTeleport = new ObjectOpenHashSet<>();

    public int teleDelay = 0;

    public boolean shouldRender;

    public boolean prevShouldRender;

    public Frequency frequency;

    public List<Frequency> publicCache = new ArrayList<>();
    public List<Frequency> privateCache = new ArrayList<>();

    /**
     * This teleporter's current status.
     */
    public byte status = 0;

    private TileComponentChunkLoader<TileEntityTeleporter> chunkLoaderComponent;

    private EnergyInventorySlot energySlot;

    public TileEntityTeleporter() {
        super(MekanismBlocks.TELEPORTER);
        doAutoSync = true;
        chunkLoaderComponent = new TileComponentChunkLoader<>(this);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 153, 7));
        return builder.build();
    }

    public static void alignPlayer(ServerPlayerEntity player, Coord4D coord) {
        Direction side = null;
        float yaw = player.rotationYaw;
        BlockPos upperPos = coord.getPos().up();
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
    public void onUpdate() {
        if (teleportBounds == null) {
            resetBounds();
        }

        if (!isRemote()) {
            FrequencyManager manager = getManager(frequency);
            if (manager != null) {
                if (frequency != null && !frequency.valid) {
                    frequency = manager.validateFrequency(getSecurity().getOwnerUUID(), Coord4D.get(this), frequency);
                }
                if (frequency != null) {
                    frequency = manager.update(Coord4D.get(this), frequency);
                }
            } else {
                frequency = null;
            }

            status = canTeleport();
            if (MekanismUtils.canFunction(this) && status == 1 && teleDelay == 0) {
                teleport();
            }
            if (teleDelay == 0 && !didTeleport.isEmpty()) {
                cleanTeleportCache();
            }

            shouldRender = status == 1 || status > 4;
            if (shouldRender != prevShouldRender) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                //This also means the comparator output changed so notify the neighbors we have a change
                MekanismUtils.notifyLoadedNeighborsOfTileChange(world, Coord4D.get(this));
            }
            prevShouldRender = shouldRender;
            teleDelay = Math.max(0, teleDelay - 1);
        }
        energySlot.discharge(this);
    }

    @Override
    public Frequency getFrequency(FrequencyManager manager) {
        if (manager == Mekanism.securityFrequencies) {
            return getSecurity().getFrequency();
        }
        return frequency;
    }

    public Coord4D getClosest() {
        if (frequency != null) {
            return frequency.getClosestCoords(Coord4D.get(this));
        }
        return null;
    }

    public void setFrequency(String name, boolean publicFreq) {
        FrequencyManager manager = getManager(new Frequency(name, null).setPublic(publicFreq));
        manager.deactivate(Coord4D.get(this));
        for (Frequency freq : manager.getFrequencies()) {
            if (freq.name.equals(name)) {
                frequency = freq;
                frequency.activeCoords.add(Coord4D.get(this));
                MekanismUtils.saveChunk(this);
                return;
            }
        }
        Frequency freq = new Frequency(name, getSecurity().getOwnerUUID()).setPublic(publicFreq);
        freq.activeCoords.add(Coord4D.get(this));
        manager.addFrequency(freq);
        frequency = freq;
        MekanismUtils.saveChunk(this);
    }

    public FrequencyManager getManager(Frequency freq) {
        if (getSecurity().getOwnerUUID() == null || freq == null) {
            return null;
        }
        if (freq.isPublic()) {
            return Mekanism.publicTeleporters;
        } else if (!Mekanism.privateTeleporters.containsKey(getSecurity().getOwnerUUID())) {
            FrequencyManager manager = new FrequencyManager(Frequency.class, Frequency.TELEPORTER, getSecurity().getOwnerUUID());
            Mekanism.privateTeleporters.put(getSecurity().getOwnerUUID(), manager);
            if (!isRemote()) {
                manager.createOrLoad();
            }
        }

        return Mekanism.privateTeleporters.get(getSecurity().getOwnerUUID());
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (!isRemote() && frequency != null) {
            FrequencyManager manager = getManager(frequency);
            if (manager != null) {
                manager.deactivate(Coord4D.get(this));
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (!isRemote()) {
            if (frequency != null) {
                FrequencyManager manager = getManager(frequency);
                if (manager != null) {
                    manager.deactivate(Coord4D.get(this));
                }
            }
        }
    }

    public void cleanTeleportCache() {
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

    public void resetBounds() {
        teleportBounds = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
    }

    /**
     * @return 1: yes, 2: no frame, 3: no link found, 4: not enough electricity
     */
    public byte canTeleport() {
        if (!hasFrame()) {
            return 2;
        }
        if (getClosest() == null) {
            return 3;
        }
        List<Entity> entitiesInPortal = getToTeleport();
        Coord4D closestCoords = getClosest();
        int electricityNeeded = 0;
        for (Entity entity : entitiesInPortal) {
            electricityNeeded += calculateEnergyCost(entity, closestCoords);
        }
        if (getEnergy() < electricityNeeded) {
            return 4;
        }
        return 1;
    }

    public void teleport() {
        if (isRemote()) {
            return;
        }
        List<Entity> entitiesInPortal = getToTeleport();
        Coord4D closestCoords = getClosest();
        if (closestCoords == null) {
            return;
        }
        for (Entity entity : entitiesInPortal) {
            World teleWorld = ServerLifecycleHooks.getCurrentServer().getWorld(closestCoords.dimension);
            TileEntityTeleporter teleporter = MekanismUtils.getTileEntity(TileEntityTeleporter.class, teleWorld, closestCoords.getPos());

            if (teleporter != null) {
                teleporter.didTeleport.add(entity.getUniqueID());
                teleporter.teleDelay = 5;
                teleportEntityTo(entity, closestCoords, teleporter);
                if (entity instanceof ServerPlayerEntity) {
                    alignPlayer((ServerPlayerEntity) entity, closestCoords);
                }
                for (Coord4D coords : frequency.activeCoords) {
                    //TODO: Check
                    Mekanism.packetHandler.sendToAllTracking(new PacketPortalFX(coords), world, coords.getPos());
                }
                setEnergy(getEnergy() - calculateEnergyCost(entity, closestCoords));
                world.playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, entity.getSoundCategory(), 1.0F, 1.0F, false);
            }
        }
    }

    public static void teleportEntityTo(Entity entity, Coord4D coord, TileEntityTeleporter teleporter) {
        if (entity.dimension != coord.dimension) {
            entity.changeDimension(coord.dimension, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity repositionedEntity = repositionEntity.apply(false);
                    repositionedEntity.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5);
                    return repositionedEntity;
                }
            });
        } else {
            entity.setPositionAndUpdate(coord.x + 0.5, coord.y + 1, coord.z + 0.5);
        }
    }

    public List<Entity> getToTeleport() {
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, teleportBounds);
        List<Entity> ret = new ArrayList<>();
        for (Entity entity : entities) {
            if (!didTeleport.contains(entity.getUniqueID())) {
                ret.add(entity);
            }
        }
        return ret;
    }

    public int calculateEnergyCost(Entity entity, Coord4D coords) {
        int energyCost = MekanismConfig.usage.teleporterBase.get();
        if (entity.world.getDimension().getType().equals(coords.dimension)) {
            int distance = (int) Math.sqrt(entity.getDistanceSq(coords.x, coords.y, coords.z));
            energyCost += distance * MekanismConfig.usage.teleporterDistance.get();
        } else {
            energyCost += MekanismConfig.usage.teleporterDimensionPenalty.get();
        }
        return energyCost;
    }

    public boolean hasFrame() {
        if (isFrame(getPos().getX() - 1, getPos().getY(), getPos().getZ()) && isFrame(getPos().getX() + 1, getPos().getY(), getPos().getZ())
            && isFrame(getPos().getX() - 1, getPos().getY() + 1, getPos().getZ()) && isFrame(getPos().getX() + 1, getPos().getY() + 1, getPos().getZ())
            && isFrame(getPos().getX() - 1, getPos().getY() + 2, getPos().getZ()) && isFrame(getPos().getX() + 1, getPos().getY() + 2, getPos().getZ())
            && isFrame(getPos().getX() - 1, getPos().getY() + 3, getPos().getZ()) && isFrame(getPos().getX() + 1, getPos().getY() + 3, getPos().getZ())
            && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ())) {
            return true;
        }
        return isFrame(getPos().getX(), getPos().getY(), getPos().getZ() - 1) && isFrame(getPos().getX(), getPos().getY(), getPos().getZ() + 1)
               && isFrame(getPos().getX(), getPos().getY() + 1, getPos().getZ() - 1) && isFrame(getPos().getX(), getPos().getY() + 1, getPos().getZ() + 1)
               && isFrame(getPos().getX(), getPos().getY() + 2, getPos().getZ() - 1) && isFrame(getPos().getX(), getPos().getY() + 2, getPos().getZ() + 1)
               && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ() - 1) && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ() + 1)
               && isFrame(getPos().getX(), getPos().getY() + 3, getPos().getZ());
    }

    public boolean isFrame(int x, int y, int z) {
        BlockState state = world.getBlockState(new BlockPos(x, y, z));
        return state.getBlock() instanceof BlockTeleporterFrame;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("frequency")) {
            frequency = new Frequency(nbtTags.getCompound("frequency"));
            frequency.valid = false;
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (frequency != null) {
            CompoundNBT frequencyTag = new CompoundNBT();
            frequency.write(frequencyTag);
            nbtTags.put("frequency", frequencyTag);
        }
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                String name = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();
                setFrequency(name, isPublic);
            } else if (type == 1) {
                String freq = PacketHandler.readString(dataStream);
                boolean isPublic = dataStream.readBoolean();
                FrequencyManager manager = getManager(new Frequency(freq, null).setPublic(isPublic));
                if (manager != null) {
                    manager.remove(freq, getSecurity().getOwnerUUID());
                }
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            if (dataStream.readBoolean()) {
                frequency = new Frequency(dataStream);
            } else {
                frequency = null;
            }

            shouldRender = dataStream.readBoolean();

            publicCache.clear();
            privateCache.clear();

            int amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                publicCache.add(new Frequency(dataStream));
            }
            amount = dataStream.readInt();
            for (int i = 0; i < amount; i++) {
                privateCache.add(new Frequency(dataStream));
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (frequency == null) {
            data.add(false);
        } else {
            data.add(true);
            frequency.write(data);
        }
        data.add(shouldRender);
        //TODO: Sync list of frequencies via a syncable list
        data.add(Mekanism.publicTeleporters.getFrequencies().size());
        for (Frequency freq : Mekanism.publicTeleporters.getFrequencies()) {
            freq.write(data);
        }

        FrequencyManager manager = getManager(new Frequency(null, null).setPublic(false));
        if (manager == null) {
            data.add(0);
        } else {
            data.add(manager.getFrequencies().size());
            for (Frequency freq : manager.getFrequencies()) {
                freq.write(data);
            }
        }
        return data;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //TODO: Do we need to override getRenderBoundingBox in all the places we do, or is overriding the TE renderer good enough with the isGlobal
        return INFINITE_EXTENT_AABB;
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

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableByte.create(() -> status, value -> status = value));
    }
}