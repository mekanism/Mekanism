package mekanism.common.lib.frequency;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.TickableFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.security.SecurityFrequency;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

/// Stores a map of Identity to Frequency (Data)
public class FrequencyLookup<FREQ extends Frequency> extends SavedData {

    public static final int MAX_FREQ_LENGTH = 16;

    private final Map<Object, FREQ> frequencies = new LinkedHashMap<>();

    @Nullable
    private final UUID ownerUUID;

    private final FrequencyType<FREQ> frequencyType;
    private final SecurityMode securityMode;

    public FrequencyLookup(FrequencyType<FREQ> frequencyType, @Nullable UUID uuid, SecurityMode securityMode) {
        this(frequencyType, uuid, securityMode, Collections.emptyList());
    }

    private FrequencyLookup(FrequencyType<FREQ> frequencyType, @Nullable UUID uuid, SecurityMode securityMode, List<FREQ> loadedFrequencies) {
        this.frequencyType = frequencyType;
        ownerUUID = uuid;
        this.securityMode = securityMode;
        for (FREQ loadedFrequency : loadedFrequencies) {
            frequencies.put(loadedFrequency.getKey(), loadedFrequency);
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public FrequencyType<FREQ> getFrequencyType() {
        return frequencyType;
    }

    public boolean remove(Object key, UUID ownerUUID) {
        FREQ freq = getFrequency(key);
        if (freq != null && freq.ownerMatches(ownerUUID)) {
            freq.onRemove();
            frequencies.remove(key);
            setDirty();
            return true;
        }
        return false;
    }

    public void deactivate(@Nullable Frequency freq, Level level, BlockEntity tile) {
        if (freq != null && freq.onDeactivate(level, tile)) {
            setDirty();
        }
    }

    public FREQ validateAndUpdate(Level level, BlockEntity tile, FREQ freq) {
        FREQ storedFreq = frequencies.get(freq.getKey());
        if (storedFreq == null) {
            freq.setValid(true);
            setDirty();
            frequencies.put(freq.getKey(), freq);
            storedFreq = freq;
        }
        if (storedFreq.update(level, tile)) {
            setDirty();
        }
        return storedFreq;
    }

    public Collection<FREQ> getFrequencies() {
        if (securityMode == SecurityMode.TRUSTED && ownerUUID != null) {
            //TODO: Try to come up with a better way of doing this that allows us to cache this
            FrequencyLookup<SecurityFrequency> securityLookup = FrequencyTypes.SECURITY.getLookup(null, SecurityMode.PUBLIC);
            if (securityLookup == null) {
                return Collections.emptyList();
            }
            List<FREQ> trustedFrequencies = new ArrayList<>(frequencies.values());
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

    @Nullable
    public FREQ getFrequency(@Nullable Object key) {
        return key == null ? null : frequencies.get(key);
    }

    public FREQ getOrCreateFrequency(FrequencyIdentity identity, @Nullable UUID ownerUUID) {
        FREQ freq = frequencies.get(identity.key());
        if (freq == null) {
            freq = frequencyType.create(identity.key(), ownerUUID, identity.securityMode());
            frequencies.put(identity.key(), freq);
            setDirty();
        }
        return freq;
    }

    public void addFrequency(FREQ freq) {
        frequencies.put(freq.getKey(), freq);
        setDirty();
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
            setDirty();
        }
    }

    public static Identifier getId(@Nullable UUID ownerUUID, SecurityMode securityMode, FrequencyType<?> frequencyType) {
        StringBuilder path = new StringBuilder("frequency/");
        path.append(frequencyType.getName()).append("/");
        if (ownerUUID != null) {
            path.append(ownerUUID.toString().toLowerCase(Locale.ROOT)).append("/");
        }
        //TODO - 26.1: Do we want to cache this?
        path.append(securityMode.name().toLowerCase(Locale.ROOT));
        return Mekanism.rl(path.toString());
    }

    private static final Consumer<String> frequencyError = err -> Mekanism.logger.error("Failed to load some frequencies: {}", err);

    public static <FREQ extends Frequency> Codec<Pair<Optional<UUID>, List<FREQ>>> baseCodec(FrequencyType<FREQ> frequencyType) {
        return RecordCodecBuilder.create(instance -> instance.group(
              UUIDUtil.CODEC.optionalFieldOf(SerializationConstants.OWNER_UUID).forGetter(Pair::getFirst),
              frequencyType.codec().listOf().promotePartial(frequencyError).fieldOf(SerializationConstants.FREQUENCY_LIST).forGetter(Pair::getSecond)
        ).apply(instance, Pair::new));
    }

    public static <FREQ extends Frequency> Codec<FrequencyLookup<FREQ>> codec(FrequencyType<FREQ> frequencyType, Codec<Pair<Optional<UUID>, List<FREQ>>> baseCodec, SecurityMode securityMode) {
        return baseCodec.xmap(
              pair -> new FrequencyLookup<>(frequencyType, pair.getFirst().orElse(null), securityMode, pair.getSecond()),
              lookup -> Pair.of(Optional.ofNullable(lookup.getOwnerUUID()), new ArrayList<>(lookup.frequencies.values()))
        );
    }
}