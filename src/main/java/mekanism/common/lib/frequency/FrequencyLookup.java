package mekanism.common.lib.frequency;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.TickableFrequency;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/// Stores a map of Identity to Frequency (Data)
public class FrequencyLookup<FREQ extends Frequency> {

    public static final int MAX_FREQ_LENGTH = 16;

    private static boolean loaded;

    private final Map<Object, FREQ> frequencies = new LinkedHashMap<>();

    private final UUID ownerUUID;

    private final FrequencyType<FREQ> frequencyType;
    private final SecurityMode securityMode;

    public FrequencyLookup(FrequencyType<FREQ> frequencyType, UUID uuid, SecurityMode securityMode) {
        this(frequencyType, uuid, securityMode, Collections.emptyList());
    }

    private FrequencyLookup(FrequencyType<FREQ> frequencyType, UUID uuid, SecurityMode securityMode, List<FREQ> loadedFrequencies) {
        this.frequencyType = frequencyType;
        ownerUUID = uuid;
        this.securityMode = securityMode;
        for (FREQ loadedFrequency : loadedFrequencies) {
            frequencies.put(loadedFrequency.getKey(), loadedFrequency);
        }
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public FrequencyType<FREQ> getFrequencyType() {
        return frequencyType;
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
            FrequencyTypes.init();
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
            FrequencyLookup<SecurityFrequency> securityLookup = FrequencyTypes.SECURITY.getLookup(null, SecurityMode.PUBLIC);
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
            dirty |= ((TickableFrequency) freq).tick(tickingNormally);
        }
        if (dirty) {
            markDirty();
        }
    }

    public static Identifier getId(@Nullable UUID ownerUUID, SecurityMode securityMode, FrequencyType<?> frequencyType) {
        StringBuilder path = new StringBuilder();
        if (ownerUUID != null) {
            path.append(ownerUUID).append("/");
        }
        path.append(frequencyType.getName()).append("/");
        if (securityMode != SecurityMode.PUBLIC) {
            path.append(securityMode.name()).append("/");
        }
        path.append("FrequencyHandler");
        return Mekanism.rl(path.toString());
    }

    public String getName() {
        String owner = ownerUUID == null ? "" : ownerUUID + "_";
        if (securityMode != SecurityMode.PUBLIC) {
            return owner + frequencyType.getName() + securityMode.name() + "FrequencyHandler";
        }
        return owner + frequencyType.getName() + "FrequencyHandler";
    }

    private static final Consumer<String> frequencyError = err -> Mekanism.logger.error("Failed to load some frequencies: {}", err);

    public static <FREQ extends Frequency> Codec<Pair<UUID, List<FREQ>>> baseCodec(FrequencyType<FREQ> frequencyType) {
        return RecordCodecBuilder.create(instance -> instance.group(
              UUIDUtil.CODEC.fieldOf(SerializationConstants.OWNER_UUID).forGetter(Pair::getFirst),
              frequencyType.codec().listOf().promotePartial(frequencyError).fieldOf(SerializationConstants.FREQUENCY_LIST).forGetter(Pair::getSecond)
        ).apply(instance, Pair::new));
    }

    public static <FREQ extends Frequency> Codec<FrequencyLookup<FREQ>> codec(FrequencyType<FREQ> frequencyType, Codec<Pair<UUID, List<FREQ>>> baseCodec, SecurityMode securityMode) {
        return baseCodec.xmap(
              pair -> new FrequencyLookup<>(frequencyType, pair.getFirst(), securityMode, pair.getSecond()),
              lookup -> Pair.of(lookup.getOwnerUUID(), new ArrayList<>(lookup.frequencies.values()))
        );
    }
}