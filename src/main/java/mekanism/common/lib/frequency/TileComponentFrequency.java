package mekanism.common.lib.frequency;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFrequency;
import mekanism.common.inventory.container.sync.list.SyncableFrequencyList;
import mekanism.common.lib.CustomObjectToObjectArrayMap;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileComponentFrequency implements ITileComponent {

    private static final AtomicInteger OFFSET = new AtomicInteger(0);

    private final TileEntityMekanism tile;

    private final CustomObjectToObjectArrayMap<FrequencyType<?>, FrequencyData> nonSecurityFrequencies = new CustomObjectToObjectArrayMap<>();
    @Nullable
    private FrequencyData securityFrequency = null;

    private final Map<SecurityMode, Map<FrequencyType<?>, List<? extends Frequency>>> frequencyCache = new EnumMap<>(SecurityMode.class);
    private final int tickOffset;

    private boolean needsSave;
    private boolean needsNotify;

    //Method refs to allocate once and reuse in hot path
    private final BiConsumer<FrequencyType<?>, FrequencyData> updateFrequencyRef = this::updateFrequency;

    public TileComponentFrequency(TileEntityMekanism tile) {
        this.tile = tile;
        this.tickOffset = OFFSET.getAndIncrement() % 5;
        tile.addComponent(this);
    }

    public boolean hasCustomFrequencies() {
        return !nonSecurityFrequencies.isEmpty();
    }

    public Set<FrequencyType<?>> getCustomFrequencies() {
        return nonSecurityFrequencies.keySet();
    }

    public void tickServer(Level level, BlockPos pos) {
        if (level.getGameTime() % 5 == tickOffset) {
            if (securityFrequency != null) {
                updateFrequency(FrequencyType.SECURITY, securityFrequency);
            }
            nonSecurityFrequencies.forEach(updateFrequencyRef);
        }
        if (needsNotify) {
            tile.invalidateCapabilitiesFull();
            WorldUtils.notifyLoadedNeighborsOfTileChange(level, pos);
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
        FrequencyData value = new FrequencyData(needsSync, needsListCache, notifyNeighbors);
        if (type == FrequencyType.SECURITY) {
            securityFrequency = value;
        } else {
            nonSecurityFrequencies.put(type, value);
        }
    }

    @Nullable
    public <FREQ extends Frequency> FREQ getFrequency(FrequencyType<FREQ> type) {
        FrequencyData frequencyData = getFrequencyData(type);
        if (frequencyData == null) {
            return null;
        }
        //noinspection unchecked
        return (FREQ) frequencyData.selectedFrequency;
    }

    @Nullable
    private <FREQ extends Frequency> FrequencyData getFrequencyData(FrequencyType<FREQ> type) {
        return type == FrequencyType.SECURITY ? securityFrequency : nonSecurityFrequencies.get(type);
    }

    public <FREQ extends Frequency> void unsetFrequency(FrequencyType<FREQ> type) {
        unsetFrequency(type, getFrequencyData(type));
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

    @SuppressWarnings("unchecked")
    private <FREQ extends Frequency> List<FREQ> getCache(SecurityMode securityMode, FrequencyType<FREQ> type) {
        return (List<FREQ>) getCache(securityMode).computeIfAbsent(type, t -> new ArrayList<>());
    }

    public <FREQ extends Frequency> void setFrequencyFromData(FrequencyType<FREQ> type, FrequencyIdentity data, UUID player) {
        if (player != null) {
            FrequencyData frequencyData = getFrequencyData(type);
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
            FrequencyData frequencyData = getFrequencyData(type);
            if (frequencyData != null) {
                setNeedsNotify(frequencyData);
            }
        }
    }

    private <FREQ extends Frequency> void updateFrequency(FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData.selectedFrequency != null) {
            if (frequencyData.selectedFrequency.isValid()) {
                boolean unsetFrequency = frequencyData.selectedFrequency.isRemoved();
                //Note: Security frequencies can only be public, and security means something different on them. So even if it is set as trusted, we don't need to check this
                if (!unsetFrequency && type != FrequencyType.SECURITY && frequencyData.selectedFrequency.getSecurity() == SecurityMode.TRUSTED) {
                    //If we aren't unsetting the frequency, check if it is a trusted frequency that we no longer have access to
                    UUID ownerUUID = tile.getOwnerUUID();
                    if (ownerUUID != null && !frequencyData.selectedFrequency.ownerMatches(ownerUUID)) {
                        SecurityFrequency security = FrequencyType.SECURITY.getManager(null, SecurityMode.PUBLIC).getFrequency(frequencyData.selectedFrequency.getOwner());
                        unsetFrequency = security != null && !security.isTrusted(ownerUUID);
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
        return SerializationConstants.COMPONENT_FREQUENCY;
    }

    @Override
    public void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        if (!tile.isRemote()) {
            for (FrequencyType<?> key : nonSecurityFrequencies.keySet()) {
                setFrequencyFromComponent(input, key);
            }
        }
    }

    private <FREQ extends Frequency> void setFrequencyFromComponent(BlockEntity.DataComponentInput input, FrequencyType<FREQ> type) {
        DataComponentType<FrequencyAware<FREQ>> frequencyComponent = MekanismDataComponents.getFrequencyComponent(type);
        if (frequencyComponent != null) {
            FrequencyAware<?> frequencyAware = input.get(frequencyComponent);
            if (frequencyAware != null && frequencyAware.identity().isPresent()) {
                //TODO - 1.21: Do we need to be using the player placing it instead of the existing owner?
                // Maybe, or at least use the owner of the block?
                setFrequencyFromData(type, frequencyAware.identity().get(), frequencyAware.getOwner());
            }
        }
    }

    @Override
    public void addRemapEntries(List<DataComponentType<?>> remapEntries) {
        for (FrequencyType<?> type : nonSecurityFrequencies.keySet()) {
            DataComponentType<? extends FrequencyAware<?>> frequencyComponent = MekanismDataComponents.getFrequencyComponent(type);
            if (frequencyComponent != null && !remapEntries.contains(frequencyComponent)) {
                remapEntries.add(frequencyComponent);
            }
            if (type == FrequencyType.QIO && !remapEntries.contains(MekanismDataComponents.COLOR.get())) {
                remapEntries.add(MekanismDataComponents.COLOR.get());
            }
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
            collectFrequencyComponents(builder, entry.getKey(), entry.getValue());
        }
    }

    private <FREQ extends Frequency> void collectFrequencyComponents(DataComponentMap.Builder builder, FrequencyType<FREQ> type, FrequencyData frequencyData) {
        if (frequencyData.selectedFrequency != null) {
            DataComponentType<FrequencyAware<FREQ>> frequencyComponent = MekanismDataComponents.getFrequencyComponent(type);
            if (frequencyComponent != null) {
                builder.set(frequencyComponent, new FrequencyAware<>((FREQ) frequencyData.selectedFrequency));
                //TODO: Do we want to support multiple frequency types each having a colored frequency?
                // Currently we only really need the QIO to support it, as the other colorable frequency (teleporters)
                // don't need the item to be aware of the color
                if (type == FrequencyType.QIO && frequencyData.selectedFrequency instanceof IColorableFrequency colorableFrequency) {
                    builder.set(MekanismDataComponents.COLOR, colorableFrequency.getColor());
                }
            }
        }
    }

    @Override
    public void deserialize(CompoundTag frequencyNBT, HolderLookup.Provider provider) {
        if (securityFrequency != null) {
            deserializeFrequency(provider, frequencyNBT, FrequencyType.SECURITY, securityFrequency);
        }
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
            FrequencyType<?> type = entry.getKey();
            deserializeFrequency(provider, frequencyNBT, type, entry.getValue());
        }
    }

    private static void deserializeFrequency(HolderLookup.Provider provider, CompoundTag frequencyNBT, FrequencyType<?> type, FrequencyData frequencyData) {
        if (frequencyNBT.contains(type.getName(), Tag.TAG_COMPOUND)) {
            frequencyData.setFrequency(type.create(provider, frequencyNBT.getCompound(type.getName())));
        }
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        DynamicOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        CompoundTag frequencyNBT = new CompoundTag();
        if (securityFrequency != null) {
            serializeFrequency(ops, FrequencyType.SECURITY, securityFrequency, frequencyNBT);
        }
        for (Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
            serializeFrequency(ops, entry.getKey(), entry.getValue(), frequencyNBT);
        }
        return frequencyNBT;
    }

    private static void serializeFrequency(DynamicOps<Tag> ops, FrequencyType<?> type, FrequencyData frequencyData, CompoundTag frequencyNBT) {
        Frequency frequency = frequencyData.selectedFrequency;
        if (frequency != null) {
            DataResult<Tag> encodedIdentity = type.getIdentitySerializer().codec().encodeStart(ops, frequency.getIdentity());
            if (encodedIdentity.isSuccess()) {
                //Note: While we save the full frequency data, and do make some use of it in reading
                // in general this isn't needed and won't be used as the frequency will be grabbed
                // from the frequency manager
                frequencyNBT.put(frequency.getType().getName(), encodedIdentity.getOrThrow());
            } else {
                encodedIdentity.ifError(error -> Mekanism.logger.warn("Failed to serialize frequency identity: {}", error.message()));
            }
        }
    }

    public void readConfiguredFrequencies(HolderLookup.Provider provider, Player player, CompoundTag data) {
        if (hasCustomFrequencies() && data.contains(getComponentKey(), Tag.TAG_COMPOUND)) {
            RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
            CompoundTag frequencyNBT = data.getCompound(getComponentKey());
            for (Map.Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
                FrequencyType<?> type = entry.getKey();
                //Don't allow transferring security data via config cards
                if (type == FrequencyType.SECURITY) {
                    continue; // should no longer happen
                }
                if (frequencyNBT.contains(type.getName(), Tag.TAG_COMPOUND)) {
                    CompoundTag frequencyData = frequencyNBT.getCompound(type.getName());
                    DataResult<FrequencyIdentity> decodedIdentity = type.getIdentitySerializer().codec().parse(registryOps, frequencyData);
                    if (decodedIdentity.isSuccess()) {
                        FrequencyIdentity identity = decodedIdentity.getOrThrow();
                        if (identity.ownerUUID() != null) {
                            if (identity.securityMode() == SecurityMode.PUBLIC || identity.ownerUUID().equals(player.getUUID())) {
                                //If the frequency is public or the player is the owner allow setting the frequency
                                setFrequencyFromData(type, identity, identity.ownerUUID(), entry.getValue());
                            }
                            continue;
                        }
                    } else {
                        decodedIdentity.ifError(error -> Mekanism.logger.warn("Failed to deserialize frequency identity: {}", error.message()));
                    }
                }
                //If our stored data doesn't have a frequency for the specific type or there was some issue parsing the data, unset the frequency
                unsetFrequency(type, entry.getValue());
            }
        }
    }

    public void writeConfiguredFrequencies(HolderLookup.Provider provider, CompoundTag data) {
        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        CompoundTag frequencyNBT = new CompoundTag();
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
            FrequencyType<?> type = entry.getKey();
            Frequency frequency = entry.getValue().selectedFrequency;
            if (frequency != null && type != FrequencyType.SECURITY) {
                //Don't allow transferring security data via config cards
                DataResult<Tag> encoded = type.getIdentitySerializer().codec().encodeStart(registryOps, frequency.getIdentity());
                if (encoded.isSuccess()) {
                    frequencyNBT.put(type.getName(), encoded.getOrThrow());
                } else {
                    encoded.ifError(error -> Mekanism.logger.warn("Failed to encode frequency identity: {}", error.message()));
                }
            }
        }
        if (!frequencyNBT.isEmpty()) {
            data.put(getComponentKey(), frequencyNBT);
        }
    }

    @Override
    public void invalidate() {
        if (!tile.isRemote()) {
            for (Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
                deactivate(entry.getKey(), entry.getValue());
            }
            if (securityFrequency != null) {
                deactivate(FrequencyType.SECURITY, securityFrequency);
            }
        }
    }

    @Override
    public void trackForMainContainer(MekanismContainer container) {
        if (securityFrequency != null) {
            trackFrequencyForMainContainer(container, securityFrequency, FrequencyType.SECURITY);
        }
        for (Map.Entry<FrequencyType<?>, FrequencyData> entry : nonSecurityFrequencies.entrySet()) {
            FrequencyData data = entry.getValue();
            trackFrequencyForMainContainer(container, data, entry.getKey());
        }
    }

    private void trackFrequencyForMainContainer(MekanismContainer container, FrequencyData data, FrequencyType<?> key) {
        if (data.needsContainerSync) {
            container.track(SyncableFrequency.create((FrequencyType<Frequency>) key, () -> data.selectedFrequency, data::setFrequency));
        }
        if (data.needsListCache) {
            track(container, key);
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
        if (container.getLevel().isClientSide()) {
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