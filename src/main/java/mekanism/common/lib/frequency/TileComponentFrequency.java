package mekanism.common.lib.frequency;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.NBTConstants;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentFrequency implements ITileComponent {

    private final TileEntityMekanism tile;

    private final Map<FrequencyType<?>, FrequencyData> supportedFrequencies = new LinkedHashMap<>();

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
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
            updateFrequency(entry.getKey(), entry.getValue());
        }
        if (needsNotify) {
            tile.invalidateCapabilitiesFull();
            WorldUtils.notifyLoadedNeighborsOfTileChange(tile.getLevel(), tile.getBlockPos());
            needsNotify = false;
        }
        if (needsSave) {
            //Mark the entire tile as dirty as maybe the frequency having changed means we need to update
            // comparators if the comparators are based on data stored in the frequency
            //TODO: Make it so that we only update comparators and stuff if we needed to
            tile.setChanged();
            needsSave = false;
        }
    }

    public void track(FrequencyType<?> type, boolean needsSync, boolean needsListCache, boolean notifyNeighbors) {
        supportedFrequencies.put(type, new FrequencyData(needsSync, needsListCache, notifyNeighbors));
    }

    @Nullable
    public <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        FrequencyData frequencyData = supportedFrequencies.get(type);
        if (frequencyData == null) {
            return null;
        }
        return (FREQ) frequencyData.selectedFrequency;
    }

    public <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type) {
        unsetFrequency(type, supportedFrequencies.get(type));
    }

    private <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData != null && frequencyData.selectedFrequency != null) {
            deactivate(type, frequencyData);
            frequencyData.clearFrequency();
            setNeedsNotify(frequencyData);
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
            FrequencyData frequencyData = supportedFrequencies.get(type);
            if (frequencyData != null) {
                setFrequencyFromData(type, data, player, frequencyData);
            }
        }
    }

    private <FREQ extends Frequency> void setFrequencyFromData(FrequencyType<FREQ> type, FrequencyIdentity data, @NotNull UUID player, FrequencyData frequencyData) {
        Frequency oldFrequency = frequencyData.selectedFrequency;
        FrequencyManager<FREQ> manager = type.getManager(data, player);
        FREQ freq = manager.getOrCreateFrequency(data, player);
        if (!freq.equals(oldFrequency)) {
            //If the frequency being set isn't the existing frequency, then deactivate the old one
            // and update the tile to be using the new one
            manager.deactivate(oldFrequency, tile);
            freq.update(tile);
            frequencyData.setFrequency(freq);
            setNeedsNotify(frequencyData);
        }
    }

    public void removeFrequencyFromData(FrequencyType<?> type, FrequencyIdentity data, UUID player) {
        FrequencyManager<?> manager = type.getManager(data, player);
        if (manager != null && manager.remove(data.key(), player)) {
            setNeedsNotify(supportedFrequencies.get(type));
        }
    }

    private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData.selectedFrequency != null) {
            if (frequencyData.selectedFrequency.isValid()) {
                if (frequencyData.selectedFrequency.isRemoved()) {
                    FrequencyManager<FREQ> manager = type.getFrequencyManager((FREQ) frequencyData.selectedFrequency);
                    if (manager != null) {
                        manager.deactivate(frequencyData.selectedFrequency, tile);
                    }
                    frequencyData.clearFrequency();
                    setNeedsNotify(frequencyData);
                }
                //Note: We don't need to update the frequency for this block as in cases when it isn't invalid we do it immediately
            } else {
                FREQ frequency = (FREQ) frequencyData.selectedFrequency;
                FrequencyManager<FREQ> manager = type.getFrequencyManager(frequency);
                if (manager == null) {
                    frequencyData.clearFrequency();
                } else {
                    frequencyData.setFrequency(manager.validateAndUpdate(tile, frequency));
                }
                setNeedsNotify(frequencyData);
            }
        }
    }

    private void setNeedsNotify(FrequencyData data) {
        if (data.notifyNeighbors) {
            needsNotify = true;
        }
        needsSave = true;
    }

    private <FREQ extends Frequency> void deactivate(FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData.selectedFrequency != null) {
            FrequencyManager<FREQ> manager = type.getFrequencyManager((FREQ) frequencyData.selectedFrequency);
            if (manager != null) {
                manager.deactivate(frequencyData.selectedFrequency, tile);
            }
        }
    }

    @Override
    public void read(CompoundTag nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_FREQUENCY, Tag.TAG_COMPOUND)) {
            CompoundTag frequencyNBT = nbtTags.getCompound(NBTConstants.COMPONENT_FREQUENCY);
            for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
                FrequencyType<?> type = entry.getKey();
                if (frequencyNBT.contains(type.getName(), Tag.TAG_COMPOUND)) {
                    Frequency frequency = type.create(frequencyNBT.getCompound(type.getName()));
                    frequency.setValid(false);
                    entry.getValue().setFrequency(frequency);
                }
            }
        }
    }

    @Override
    public void write(CompoundTag nbtTags) {
        CompoundTag frequencyNBT = new CompoundTag();
        for (FrequencyData frequencyData : supportedFrequencies.values()) {
            Frequency frequency = frequencyData.selectedFrequency;
            if (frequency != null) {
                //TODO: Can this be transitioned over to frequency.serializeIdentityWithOwner()
                CompoundTag frequencyTag = new CompoundTag();
                frequency.writeComponentData(frequencyTag);
                //Note: While we save the full frequency data, and do make some use of it in reading
                // in general this isn't needed and won't be used as the frequency will be grabbed
                // from the frequency manager
                frequencyNBT.put(frequency.getType().getName(), frequencyTag);
            }
        }
        nbtTags.put(NBTConstants.COMPONENT_FREQUENCY, frequencyNBT);
    }

    public void readConfiguredFrequencies(Player player, CompoundTag data) {
        if (hasCustomFrequencies() && data.contains(NBTConstants.COMPONENT_FREQUENCY, Tag.TAG_COMPOUND)) {
            CompoundTag frequencyNBT = data.getCompound(NBTConstants.COMPONENT_FREQUENCY);
            for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
                FrequencyType<?> type = entry.getKey();
                if (type != FrequencyType.SECURITY) {
                    //Don't allow transferring security data via config cards
                    if (frequencyNBT.contains(type.getName(), Tag.TAG_COMPOUND)) {
                        CompoundTag frequencyData = frequencyNBT.getCompound(type.getName());
                        if (frequencyData.hasUUID(NBTConstants.OWNER_UUID)) {
                            FrequencyIdentity identity = FrequencyIdentity.load(type, frequencyData);
                            if (identity != null) {
                                UUID owner = frequencyData.getUUID(NBTConstants.OWNER_UUID);
                                if (identity.isPublic() || owner.equals(player.getUUID())) {
                                    //If the frequency is public or the player is the owner allow setting the frequency
                                    setFrequencyFromData(type, identity, owner, entry.getValue());
                                }
                                continue;
                            }
                        }
                    }
                    //If our stored data doesn't have a frequency for the specific type or there was some issue parsing the data, unset the frequency
                    unsetFrequency(type, entry.getValue());
                }
            }
        }
    }

    public void writeConfiguredFrequencies(CompoundTag data) {
        CompoundTag frequencyNBT = new CompoundTag();
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
            Frequency frequency = entry.getValue().selectedFrequency;
            if (frequency != null && entry.getKey() != FrequencyType.SECURITY) {
                //Don't allow transferring security data via config cards
                frequencyNBT.put(entry.getKey().getName(), frequency.serializeIdentityWithOwner());
            }
        }
        if (!frequencyNBT.isEmpty()) {
            data.put(NBTConstants.COMPONENT_FREQUENCY, frequencyNBT);
        }
    }

    @Override
    public void invalidate() {
        if (!tile.isRemote()) {
            supportedFrequencies.forEach(this::deactivate);
        }
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
            FrequencyData data = entry.getValue();
            if (data.needsContainerSync) {
                container.track(SyncableFrequency.create(() -> data.selectedFrequency, data::setFrequency));
            }
            if (data.needsListCache) {
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
            container.track(SyncableFrequencyList.create(() -> type.getManagerWrapper().getPublicManager().getFrequencies(), value -> publicCache.put(type, value)));
            //Note: We take advantage of the fact that containers are one to one even on the server, and sync
            // the private frequencies of the player who opened the container rather than the private
            // frequencies of the owner of the tile
            container.track(SyncableFrequencyList.create(() -> type.getManagerWrapper().getPrivateManager(container.getPlayerUUID()).getFrequencies(),
                  value -> privateCache.put(type, value)));
        }
    }

    private static final class FrequencyData {

        private final boolean needsContainerSync;
        private final boolean needsListCache;
        private final boolean notifyNeighbors;
        @Nullable
        private Frequency selectedFrequency;

        private FrequencyData(boolean needsContainerSync, boolean needsListCache, boolean notifyNeighbors) {
            this.needsContainerSync = needsContainerSync;
            this.needsListCache = needsListCache;
            this.notifyNeighbors = notifyNeighbors;
        }

        public void setFrequency(@Nullable Frequency frequency) {
            selectedFrequency = frequency;
        }

        public void clearFrequency() {
            setFrequency(null);
        }
    }
}