package mekanism.common.frequency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.api.Coord4D;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentFrequency implements ITileComponent {

    private TileEntityMekanism tile;

    private Map<FrequencyType<?>, Frequency> heldFrequencies = new Object2ObjectOpenHashMap<>();
    private Map<FrequencyType<?>, FrequencyTrackingData> supportedFrequencies = new Object2ObjectOpenHashMap<>();

    private Map<FrequencyType<? extends Frequency>, List<? extends Frequency>> publicCache = new Object2ObjectOpenHashMap<>();
    private Map<FrequencyType<? extends Frequency>, List<? extends Frequency>> privateCache = new Object2ObjectOpenHashMap<>();

    private boolean didNotify;

    public TileComponentFrequency(TileEntityMekanism tile) {
        this.tile = tile;
        tile.addComponent(this);
    }

    @Override
    public void tick() {
        if (!tile.getWorld().isRemote()) {
            didNotify = false;

            for (FrequencyType<?> type : heldFrequencies.keySet()) {
                updateFrequency(type);
            }
        }
    }

    private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type) {
        FREQ frequency = getFrequency(type);
        Frequency lastFreq = frequency;
        FrequencyManager<FREQ> manager = getManager(type, frequency);
        if (manager != null) {
            if (frequency != null && !frequency.valid) {
                frequency = manager.validateFrequency(tile.getSecurity().getOwnerUUID(), Coord4D.get(tile), frequency);
                notifyNeighbors(type);
            }
            if (frequency != null) {
                frequency = manager.update(Coord4D.get(tile), frequency);
                if (frequency == null)
                    notifyNeighbors(type);
            }
        } else {
            frequency = null;
            if (lastFreq != null)
                notifyNeighbors(type);
        }
        setFrequency(type, frequency);
    }

    private void notifyNeighbors(FrequencyType<?> type) {
        if (!didNotify && supportedFrequencies.get(type).notifyNeighbors) {
            MekanismUtils.notifyLoadedNeighborsOfTileChange(tile.getWorld(), tile.getPos());
            tile.markDirty(false);
            didNotify = true;
        }
    }

    private void unload() {
        if (!tile.getWorld().isRemote()) {
            heldFrequencies.entrySet().forEach(entry -> deactivate(entry.getKey()));
        }
    }

    private <FREQ extends Frequency> void deactivate(FrequencyType<FREQ> type) {
        FREQ freq = getFrequency(type);
        if (freq != null) {
            FrequencyManager<FREQ> manager = getManager(type, freq);
            if (manager != null) {
                manager.deactivate(freq, Coord4D.get(tile));
            }
        }
    }

    public <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        return (FREQ) heldFrequencies.get(type);
    }

    public void setFrequency(FrequencyType<?> type, Frequency freq) {
        heldFrequencies.put(type, freq);
    }

    public void track(FrequencyType<?> type, boolean needsSync, boolean needsListCache, boolean notifyNeighbors) {
        supportedFrequencies.put(type, new FrequencyTrackingData(needsSync, needsListCache, notifyNeighbors));
    }

    public <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
        return (List<FREQ>) publicCache.computeIfAbsent(type, t -> new ArrayList<>());
    }

    public <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return (List<FREQ>) privateCache.computeIfAbsent(type, t -> new ArrayList<>());
    }

    private <FREQ extends Frequency> FrequencyManager<FREQ> getManager(FrequencyType<FREQ> type, FREQ freq) {
        if (tile.getSecurity().getOwnerUUID() == null || freq == null) {
            return null;
        }
        return type.getWrapperManager().getManager(freq);
    }

    private <FREQ extends Frequency> FrequencyManager<FREQ> getManager(FrequencyType<FREQ> type, FrequencyIdentity data) {
        FrequencyManagerWrapper<FREQ> wrapper = type.getWrapperManager();
        return data.isPublic() ? wrapper.getPublicManager() : wrapper.getPrivateManager(tile.getSecurity().getOwnerUUID());
    }

    public <FREQ extends Frequency> void setFrequencyFromData(FrequencyType<FREQ> type, FrequencyIdentity data) {
        FrequencyManager<FREQ> manager = getManager(type, data);
        manager.deactivate(getFrequency(type), Coord4D.get(tile));
        FREQ freq = manager.getFrequencies().get(data.getKey());
        if (freq != null) {
            freq.activeCoords.add(Coord4D.get(tile));
            notifyNeighbors(type);
            tile.invalidateCachedCapabilities();
            setFrequency(type, freq);
            return;
        }

        freq = type.create(data.getKey(), tile.getSecurity().getOwnerUUID());
        freq.setPublic(data.isPublic());
        freq.activeCoords.add(Coord4D.get(tile));
        manager.addFrequency(freq);
        setFrequency(type, freq);
        tile.invalidateCachedCapabilities();
        notifyNeighbors(type);
    }

    public void removeFrequencyFromData(FrequencyType<?> type, FrequencyIdentity data) {
        FrequencyManager<?> manager = getManager(type, data);
        if (manager != null) {
            manager.remove(data.getKey(), tile.getSecurity().getOwnerUUID());
            tile.invalidateCachedCapabilities();
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        for (FrequencyType<?> type : supportedFrequencies.keySet()) {
            if (nbtTags.contains(type.getName(), NBT.TAG_COMPOUND)) {
                Frequency frequency = type.create(nbtTags.getCompound(type.getName()), false);
                frequency.valid = false;
                heldFrequencies.put(type, frequency);
            }
        }
    }

    @Nonnull
    @Override
    public void write(CompoundNBT nbtTags) {
        for (Frequency frequency : heldFrequencies.values()) {
            if (frequency != null) {
                CompoundNBT frequencyTag = new CompoundNBT();
                frequency.write(frequencyTag);
                nbtTags.put(frequency.getType().getName(), frequencyTag);
            }
        }
    }

    @Override
    public void invalidate() {
        unload();
    }

    @Override
    public void onChunkUnload() {
        unload();
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        for (Map.Entry<FrequencyType<?>, FrequencyTrackingData> entry : supportedFrequencies.entrySet()) {
            if (entry.getValue().needsSync) {
                container.track(SyncableFrequency.create(() -> heldFrequencies.get(entry.getKey()), value -> heldFrequencies.put(entry.getKey(), value)));
            }
            if (entry.getValue().needsListCache) {
                track(container, entry.getKey());
            }
        }
    }

    private <FREQ extends Frequency> void track(MekanismContainer container, FrequencyType<FREQ> type) {
        container.track(SyncableFrequencyList.create(() ->
              type.getWrapperManager().getPublicFrequencies(tile, getPublicCache(type)),
              value -> publicCache.put(type, value)));
        container.track(SyncableFrequencyList.create(() ->
              type.getWrapperManager().getPrivateFrequencies(tile, getPrivateCache(type)),
              value -> privateCache.put(type, value)));
    }

    @Override
    public void addToUpdateTag(CompoundNBT updateTag) {}

    @Override
    public void readFromUpdateTag(CompoundNBT updateTag) {}

    private static class FrequencyTrackingData {
        private final boolean needsSync;
        private final boolean needsListCache;
        private final boolean notifyNeighbors;

        public FrequencyTrackingData(boolean needsSync, boolean needsListCache, boolean notifyNeighbors) {
            this.needsSync = needsSync;
            this.needsListCache = needsListCache;
            this.notifyNeighbors = notifyNeighbors;
        }
    }
}
