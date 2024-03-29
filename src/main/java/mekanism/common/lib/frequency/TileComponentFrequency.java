package mekanism.common.lib.frequency;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.NBTConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
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

    private final Map<SecurityMode, Map<FrequencyType<?>, List<? extends Frequency>>> frequencyCache = new EnumMap<>(SecurityMode.class);

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

    public Set<FrequencyType<?>> getCustomFrequencies() {
        if (supportedFrequencies.containsKey(FrequencyType.SECURITY)) {
            return Sets.difference(supportedFrequencies.keySet(), Set.of(FrequencyType.SECURITY));
        }
        return supportedFrequencies.keySet();
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
        return getCache(SecurityMode.PUBLIC, type);
    }

    public <FREQ extends Frequency> List<FREQ> getPrivateCache(FrequencyType<FREQ> type) {
        return getCache(SecurityMode.PRIVATE, type);
    }

    public <FREQ extends Frequency> List<FREQ> getTrustedCache(FrequencyType<FREQ> type) {
        return getCache(SecurityMode.TRUSTED, type);
    }

    private Map<FrequencyType<?>, List<? extends Frequency>> getCache(SecurityMode securityMode) {
        return frequencyCache.computeIfAbsent(securityMode, mode -> new LinkedHashMap<>());
    }

    private <FREQ extends Frequency> List<FREQ> getCache(SecurityMode securityMode, FrequencyType<FREQ> type) {
        return (List<FREQ>) getCache(securityMode).computeIfAbsent(type, t -> new ArrayList<>());
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
        FrequencyManager<FREQ> manager = null;
        FREQ freq = null;
        if (!Objects.equals(data.ownerUUID(), player) && SecurityUtils.get().isTrusted(data.securityMode(), data.ownerUUID(), player)) {
            manager = type.getManager(data, data.ownerUUID());
            freq = manager.getFrequency(data.key());
            if (freq == null) {
                //Frequency doesn't exist, update the data to having the player as the owner
                data = new FrequencyIdentity(data.key(), data.securityMode(), player);
            }
        }
        if (freq == null) {
            //If the player is the owner, or is trying to create a new trusted frequency, create it for this player instead
            manager = type.getManager(data, player);
            freq = manager.getOrCreateFrequency(data, player);
        }
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
        FrequencyManager<?> manager = type.getManager(data, data.ownerUUID() == null ? player : data.ownerUUID());
        if (manager != null && manager.remove(data.key(), player)) {
            setNeedsNotify(supportedFrequencies.get(type));
        }
    }

    private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData.selectedFrequency != null) {
            if (frequencyData.selectedFrequency.isValid()) {
                boolean unsetFrequency = frequencyData.selectedFrequency.isRemoved();
                if (!unsetFrequency && frequencyData.selectedFrequency.getSecurity() == SecurityMode.TRUSTED) {
                    //If we aren't unsetting the frequency, check if it is a trusted frequency that we no longer have access to
                    UUID ownerUUID = tile.getOwnerUUID();
                    if (ownerUUID != null && !frequencyData.selectedFrequency.ownerMatches(ownerUUID)) {
                        SecurityFrequency security = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(frequencyData.selectedFrequency.getOwner());
                        unsetFrequency = security != null && !security.getTrustedUUIDs().contains(ownerUUID);
                    }
                }
                if (unsetFrequency) {
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
    public String getComponentKey() {
        return NBTConstants.COMPONENT_FREQUENCY;
    }

    @Override
    public void deserialize(CompoundTag frequencyNBT) {
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
            FrequencyType<?> type = entry.getKey();
            if (frequencyNBT.contains(type.getName(), Tag.TAG_COMPOUND)) {
                Frequency frequency = type.create(frequencyNBT.getCompound(type.getName()));
                frequency.setValid(false);
                entry.getValue().setFrequency(frequency);
            }
        }
    }

    @Override
    public CompoundTag serialize() {
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
        return frequencyNBT;
    }

    public void readConfiguredFrequencies(Player player, CompoundTag data) {
        if (hasCustomFrequencies() && data.contains(getComponentKey(), Tag.TAG_COMPOUND)) {
            CompoundTag frequencyNBT = data.getCompound(getComponentKey());
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
                                if (identity.securityMode() == SecurityMode.PUBLIC || owner.equals(player.getUUID())) {
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
            data.put(getComponentKey(), frequencyNBT);
        }
    }

    @Override
    public void invalidate() {
        if (!tile.isRemote()) {
            supportedFrequencies.forEach(this::deactivate);
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void trackForMainContainer(MekanismContainer container) {
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : supportedFrequencies.entrySet()) {
            FrequencyData data = entry.getValue();
            if (data.needsContainerSync) {
                container.track(SyncableFrequency.create((FrequencyType<Frequency>) entry.getKey(), () -> data.selectedFrequency, data::setFrequency));
            }
            if (data.needsListCache) {
                track(container, entry.getKey());
            }
        }
    }

    private <FREQ extends Frequency> Consumer<@NotNull List<FREQ>> getSetter(SecurityMode securityMode, FrequencyType<FREQ> type) {
        Map<FrequencyType<?>, List<? extends Frequency>> cache = getCache(securityMode);
        return value -> cache.put(type, value);
    }


    private <FREQ extends Frequency> void track(MekanismContainer container, FrequencyType<FREQ> type) {
        Consumer<@NotNull List<FREQ>> publicSetter = getSetter(SecurityMode.PUBLIC, type);
        Consumer<@NotNull List<FREQ>> privateSetter = getSetter(SecurityMode.PRIVATE, type);
        Consumer<@NotNull List<FREQ>> trustedSetter = getSetter(SecurityMode.TRUSTED, type);

        //Simplify out the is remote check. Note: It is important the client and server trackers are in the same order
        if (container.isRemote()) {
            container.track(SyncableFrequencyList.create(type, () -> getPublicCache(type), publicSetter));
            container.track(SyncableFrequencyList.create(type, () -> getPrivateCache(type), privateSetter));
            container.track(SyncableFrequencyList.create(type, () -> getTrustedCache(type), trustedSetter));
        } else {
            container.track(SyncableFrequencyList.create(type, () -> type.getManagerWrapper().getPublicManager().getFrequencies(), publicSetter));
            //Note: We take advantage of the fact that containers are one to one even on the server, and sync
            // the private frequencies of the player who opened the container rather than the private
            // frequencies of the owner of the tile
            container.track(SyncableFrequencyList.create(type, () -> type.getManagerWrapper().getPrivateManager(container.getPlayerUUID()).getFrequencies(), privateSetter));
            container.track(SyncableFrequencyList.create(type, () -> type.getManagerWrapper().getTrustedManager(container.getPlayerUUID()).getFrequencies(), trustedSetter));
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