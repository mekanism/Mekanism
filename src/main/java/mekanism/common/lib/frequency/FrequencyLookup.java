package mekanism.common.lib.frequency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// Stores a map of Identity to Frequency (Data)
public class FrequencyLookup<FREQ extends Frequency> {

    public static final int MAX_FREQ_LENGTH = 16;

    private static boolean loaded;

    private final Map<Object, FREQ> frequencies = new LinkedHashMap<>();

    private UUID ownerUUID;

    private final FrequencyType<FREQ> frequencyType;
    private SecurityMode securityMode = SecurityMode.PUBLIC;

    public FrequencyLookup(FrequencyType<FREQ> frequencyType) {
        this(frequencyType, null, SecurityMode.PUBLIC);
    }

    public FrequencyLookup(FrequencyType<FREQ> frequencyType, UUID uuid, SecurityMode securityMode) {
        this.frequencyType = frequencyType;
        ownerUUID = uuid;
        this.securityMode = securityMode;
    }

    /**
     * Note: This should only be called from the server side
     */
    public static void load() {
        if (!loaded) {
            loaded = true;
            //Ensure that the frequency types have been initialized so can add their looksups
            // This is needed as it is statically initialized, and we need to make sure that it gets initialized
            // before we try to create or load each frequency, or they won't be properly loaded/saved on servers
            // as this happens on servers before the frequency types reliably have a chance to add their lookups
            FrequencyType.init();
            allLookups.forEach(FrequencyLookup::createOrLoad);
        }
    }

    public boolean remove(Object key, UUID ownerUUID) {
        FREQ freq = getFrequency(key);
        if (freq != null && freq.ownerMatches(ownerUUID)) {
            freq.onRemove();
            frequencies.remove(key);
            markDirty();
            return true;
        }
        return false;
    }

    public void deactivate(@Nullable Frequency freq, BlockEntity tile) {
        if (freq != null && freq.onDeactivate(tile)) {
            markDirty();
        }
    }

    public FREQ validateAndUpdate(BlockEntity tile, FREQ freq) {
        FREQ storedFreq = frequencies.get(freq.getKey());
        if (storedFreq == null) {
            freq.setValid(true);
            markDirty();
            frequencies.put(freq.getKey(), freq);
            storedFreq = freq;
        }
        if (storedFreq.update(tile)) {
            markDirty();
        }
        return storedFreq;
    }

    /**
     * Note: This should only be called from the server side
     */
    public void createOrLoad() {
        if (dataHandler == null) {
            String name = getName();
            //Always associate the world with the over world as the frequencies are global
            dataHandler = MekanismSavedData.createSavedData(FrequencyDataHandler::new, name);
            dataHandler.syncLookup();
        }
    }

    public Collection<FREQ> getFrequencies() {
        if (securityMode == SecurityMode.TRUSTED && ownerUUID != null) {
            List<FREQ> trustedFrequencies = new ArrayList<>(frequencies.values());
            //TODO: Try to come up with a better way of doing this that allows us to cache this
            FrequencyLookup<SecurityFrequency> securityLookup = FrequencyType.SECURITY.getLookup(null, SecurityMode.PUBLIC);
            for (FrequencyLookup<FREQ> trustedLookup : frequencyType.getController().getTrustedLookups()) {
                if (!ownerUUID.equals(trustedLookup.ownerUUID)) {
                    //Add any frequencies that the owner has access to because of being trusted by the other player
                    SecurityFrequency frequency = securityLookup.getFrequency(trustedLookup.ownerUUID);
                    if (frequency != null && frequency.isTrusted(ownerUUID)) {
                        trustedFrequencies.addAll(trustedLookup.frequencies.values());
                    }
                }
            }
            return trustedFrequencies;
        }
        return frequencies.values();
    }

    public FREQ getFrequency(Object key) {
        return frequencies.get(key);
    }

    public FREQ getOrCreateFrequency(FrequencyIdentity identity, @Nullable UUID ownerUUID) {
        FREQ freq = frequencies.get(identity.key());
        if (freq == null) {
            freq = frequencyType.create(identity.key(), ownerUUID, identity.securityMode());
            frequencies.put(identity.key(), freq);
            markDirty();
        }
        return freq;
    }

    public void addFrequency(FREQ freq) {
        frequencies.put(freq.getKey(), freq);
        markDirty();
    }

    protected void markDirty() {
        if (dataHandler != null) {
            dataHandler.setDirty();
        }
    }

    public FrequencyType<FREQ> getType() {
        return frequencyType;
    }

    public void tickSelf(boolean tickingNormally) {
        boolean dirty = false;
        for (FREQ freq : frequencies.values()) {
            dirty |= freq.tick(tickingNormally);
        }
        if (dirty) {
            markDirty();
        }
    }

    public String getName() {
        String owner = ownerUUID == null ? "" : ownerUUID + "_";
        if (securityMode != SecurityMode.PUBLIC) {
            return owner + frequencyType.getName() + securityMode.name() + "FrequencyHandler";
        }
        return owner + frequencyType.getName() + "FrequencyHandler";
    }

    public class FrequencyDataHandler extends MekanismSavedData {

        public HashList<FREQ> loadedFrequencies;
        public UUID loadedOwner;

        public void syncLookup() {
            if (loadedFrequencies != null) {
                for (FREQ freq : loadedFrequencies) {
                    frequencies.put(freq.getKey(), freq);
                }
                ownerUUID = loadedOwner;
            }
        }

        @Override
        public void load(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
            if (nbtTags.hasUUID(SerializationConstants.OWNER_UUID)) {
                loadedOwner = nbtTags.getUUID(SerializationConstants.OWNER_UUID);
            }
            ListTag list = nbtTags.getListOrEmpty(SerializationConstants.FREQUENCY_LIST);
            loadedFrequencies = new HashList<>();
            Codec<FREQ> codec = frequencyType.codec();
            RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
            for (int i = 0; i < list.size(); i++) {
                DataResult<FREQ> parsed = codec.parse(registryOps, list.getCompoundOrEmpty(i));
                parsed.ifSuccess(loadedFrequencies::add);
                parsed.ifError(error -> Mekanism.logger.warn("Failed to deserialize frequency: {}", error.message()));
            }
        }

        @NotNull
        @Override
        public CompoundTag save(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
            if (ownerUUID != null) {
                nbtTags.putUUID(SerializationConstants.OWNER_UUID, ownerUUID);
            }
            Codec<FREQ> codec = frequencyType.codec();
            RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
            ListTag list = new ListTag(frequencies.size());
            for (FREQ freq : frequencies.values()) {
                DataResult<Tag> encoded = codec.encodeStart(registryOps, freq);
                encoded.ifSuccess(list::add);
                encoded.ifError(error -> Mekanism.logger.warn("Failed to serialize frequency: {}", error.message()));
            }
            nbtTags.put(SerializationConstants.FREQUENCY_LIST, list);
            return nbtTags;
        }
    }
}