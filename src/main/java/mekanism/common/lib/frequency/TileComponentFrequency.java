package mekanism.common.lib.frequency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentFrequency implements ITileComponent {

    private final TileEntityMekanism tile;

    private final Map<FrequencyType<?>, Frequency> heldFrequencies = new LinkedHashMap<>();
    private final Map<FrequencyType<?>, FrequencyTrackingData> supportedFrequencies = new LinkedHashMap<>();

    private final Map<FrequencyType<?>, List<? extends Frequency>> publicCache = new LinkedHashMap<>();
    private final Map<FrequencyType<?>, List<? extends Frequency>> privateCache = new LinkedHashMap<>();

    private boolean needsSave;
    private boolean needsNotify;

    public TileComponentFrequency(TileEntityMekanism tile) {
        this.tile = tile;
        tile.addComponent(this);
    }

    public boolean hasCustomFrequencies() {
        if (supportedFrequencies.containsKey(FrequencyType.SECURITY)) {
            return supportedFrequencies.size() > 1;
        }
        return !supportedFrequencies.isEmpty();
    }

    public void tickServer() {
        for (Entry<FrequencyType<?>, Frequency> entry : heldFrequencies.entrySet()) {
            //noinspection unchecked,rawtypes
            updateFrequency((FrequencyType) entry.getKey(), entry.getValue());
        }
        if (needsNotify) {
            tile.invalidateCachedCapabilities();
            WorldUtils.notifyLoadedNeighborsOfTileChange(tile.getLevel(), tile.getBlockPos());
            needsNotify = false;
        }
        if (needsSave) {
            tile.markDirty(false);
            needsSave = false;
        }
    }

    public void track(FrequencyType<?> type, boolean needsSync, boolean needsListCache, boolean notifyNeighbors) {
        supportedFrequencies.put(type, new FrequencyTrackingData(needsSync, needsListCache, notifyNeighbors));
    }

    public <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        return (FREQ) heldFrequencies.get(type);
    }

    public void setFrequency(FrequencyType<?> type, Frequency freq) {
        heldFrequencies.put(type, freq);
    }

    public <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type) {
        if (heldFrequencies.containsKey(type)) {
            deactivate(type);
            heldFrequencies.remove(type);
            setNeedsNotify(type);
        }
    }

    public <FREQ extends Frequency> List<FREQ> getPublicCache(FrequencyType<FREQ> type) {
        return getCache(publicCache, type);
    }

    public <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return getCache(privateCache, type);
    }

    private <FREQ extends Frequency> List<FREQ> getCache(Map<FrequencyType<?>, List<? extends Frequency>> cache, FrequencyType<FREQ> type) {
        return (List<FREQ>) cache.computeIfAbsent(type, t -> new ArrayList<>());
    }

    public <FREQ extends Frequency> void setFrequencyFromData(FrequencyType<FREQ> type, FrequencyIdentity data, UUID player) {
        if (player != null) {
            FREQ oldFrequency = getFrequency(type);
            FrequencyManager<FREQ> manager = type.getManager(data, player);
            FREQ freq = manager.getOrCreateFrequency(data, player);
            if (oldFrequency == null || !oldFrequency.equals(freq)) {
                //If the frequency being set isn't the existing frequency, then deactivate the old one
                // and update the tile to be using the new one
                manager.deactivate(oldFrequency, tile);
                freq.update(tile);
                setFrequency(type, freq);
                setNeedsNotify(type);
            }
        }
    }

    public void removeFrequencyFromData(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        FrequencyManager<?> manager = type.getManager(data, player);
        if (manager != null && manager.remove(data.getKey(), player)) {
            setNeedsNotify(type);
        }
    }

    private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type, FREQ frequency) {
        //TODO: Instead of doing all of this every tick try and see if we can make a system to mark frequencies as being in need of an update?
        Frequency lastFreq = frequency;
        FrequencyManager<FREQ> manager = type.getFrequencyManager(frequency);
        if (manager != null) {
            if (!frequency.isValid()) {
                frequency = manager.validateFrequency(tile, frequency);
                setNeedsNotify(type);
            }
            if (frequency != null) {
                frequency = manager.update(tile, frequency);
                if (frequency == null) {
                    setNeedsNotify(type);
                }
            }
        } else {
            frequency = null;
            if (lastFreq != null) {
                setNeedsNotify(type);
            }
        }
        if (frequency != lastFreq) {
            //If the object changed update the frequency in our table
            setFrequency(type, frequency);
        }
    }

    private void setNeedsNotify(FrequencyType<?> type) {
        if (supportedFrequencies.get(type).notifyNeighbors) {
            needsNotify = true;
        }
        needsSave = true;
    }

    private void unload() {
        if (!tile.isRemote()) {
            heldFrequencies.forEach((key, value) -> deactivate(key));
        }
    }

    private <FREQ extends Frequency> void deactivate(FrequencyType<FREQ> type) {
        FREQ freq = getFrequency(type);
        if (freq != null) {
            FrequencyManager<FREQ> manager = type.getFrequencyManager(freq);
            if (manager != null) {
                manager.deactivate(freq, tile);
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        CompoundNBT frequencyNBT;
        if (nbtTags.contains(NBTConstants.COMPONENT_FREQUENCY, NBT.TAG_COMPOUND)) {
            frequencyNBT = nbtTags.getCompound(NBTConstants.COMPONENT_FREQUENCY);
        } else {
            //TODO - 1.18: Remove this old fallback loading system
            frequencyNBT = nbtTags;
        }
        for (FrequencyType<?> type : supportedFrequencies.keySet()) {
            if (frequencyNBT.contains(type.getName(), NBT.TAG_COMPOUND)) {
                Frequency frequency = type.create(frequencyNBT.getCompound(type.getName()));
                frequency.setValid(false);
                heldFrequencies.put(type, frequency);
            }
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT frequencyNBT = new CompoundNBT();
        for (Frequency frequency : heldFrequencies.values()) {
            if (frequency != null) {
                //TODO: Can this be transitioned over to frequency.serializeIdentityWithOwner()
                CompoundNBT frequencyTag = new CompoundNBT();
                frequency.writeComponentData(frequencyTag);
                frequencyNBT.put(frequency.getType().getName(), frequencyTag);
            }
        }
        nbtTags.put(NBTConstants.COMPONENT_FREQUENCY, frequencyNBT);
    }

    public void readConfiguredFrequencies(PlayerEntity player, CompoundNBT data) {
        if (hasCustomFrequencies() && data.contains(NBTConstants.COMPONENT_FREQUENCY, NBT.TAG_COMPOUND)) {
            CompoundNBT frequencyNBT = data.getCompound(NBTConstants.COMPONENT_FREQUENCY);
            for (FrequencyType<?> type : supportedFrequencies.keySet()) {
                if (type != FrequencyType.SECURITY) {
                    //Don't allow transferring security data via config cards
                    if (frequencyNBT.contains(type.getName(), NBT.TAG_COMPOUND)) {
                        CompoundNBT frequencyData = frequencyNBT.getCompound(type.getName());
                        if (frequencyData.hasUUID(NBTConstants.OWNER_UUID)) {
                            FrequencyIdentity identity = FrequencyIdentity.load(type, frequencyData);
                            if (identity != null) {
                                UUID owner = frequencyData.getUUID(NBTConstants.OWNER_UUID);
                                if (identity.isPublic() || owner.equals(player.getUUID())) {
                                    //If the frequency is public or the player is the owner allow setting the frequency
                                    setFrequencyFromData(type, identity, owner);
                                }
                                continue;
                            }
                        }
                    }
                    //If our stored data doesn't have a frequency for the specific type or there was some issue parsing the data, unset the frequency
                    unsetFrequency(type);
                }
            }
        }
    }

    public void writeConfiguredFrequencies(CompoundNBT data) {
        CompoundNBT frequencyNBT = new CompoundNBT();
        for (Frequency frequency : heldFrequencies.values()) {
            if (frequency != null && frequency.getType() != FrequencyType.SECURITY) {
                //Don't allow transferring security data via config cards
                frequencyNBT.put(frequency.getType().getName(), frequency.serializeIdentityWithOwner());
            }
        }
        if (!frequencyNBT.isEmpty()) {
            data.put(NBTConstants.COMPONENT_FREQUENCY, frequencyNBT);
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
            if (entry.getValue().needsContainerSync) {
                container.track(SyncableFrequency.create(() -> heldFrequencies.get(entry.getKey()), value -> heldFrequencies.put(entry.getKey(), value)));
            }
            if (entry.getValue().needsListCache) {
                track(container, entry.getKey());
            }
        }
    }

    private <FREQ extends Frequency> void track(MekanismContainer container, FrequencyType<FREQ> type) {
        //Simplify out the is remote check. Note: It is important the client and server trackers are in the same order
        if (container.isRemote()) {
            container.track(SyncableFrequencyList.create(() -> getPublicCache(type), value -> publicCache.put(type, value)));
            container.track(SyncableFrequencyList.create(() -> getPrivateCache(type), value -> privateCache.put(type, value)));
        } else {
            container.track(SyncableFrequencyList.create(() -> type.getManagerWrapper().getPublicManager().getFrequencies(),
                  value -> publicCache.put(type, value)));
            //Note: We take advantage of the fact that containers are one to one even on the server, and sync
            // the private frequencies of the player who opened the container rather than the private
            // frequencies of the owner of the tile
            container.track(SyncableFrequencyList.create(() -> type.getManagerWrapper().getPrivateManager(container.getPlayerUUID()).getFrequencies(),
                  value -> privateCache.put(type, value)));
        }
    }

    private static class FrequencyTrackingData {

        private final boolean needsContainerSync;
        private final boolean needsListCache;
        private final boolean notifyNeighbors;

        public FrequencyTrackingData(boolean needsContainerSync, boolean needsListCache, boolean notifyNeighbors) {
            this.needsContainerSync = needsContainerSync;
            this.needsListCache = needsListCache;
            this.notifyNeighbors = notifyNeighbors;
        }
    }
}
