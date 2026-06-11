package mekanism.common.lib.frequency;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import org.jspecify.annotations.Nullable;

public class FrequencyController<FREQ extends Frequency> {

    private final FrequencyType<FREQ> frequencyType;
    @Nullable
    private FrequencyLookup<FREQ> publicLookup;
    @Nullable
    private Map<UUID, FrequencyLookup<FREQ>> privateLookups;
    @Nullable
    private Map<UUID, FrequencyLookup<FREQ>> trustedLookups;

    @Nullable
    private final Codec<FrequencyLookup<FREQ>> publicCodec;
    @Nullable
    private final Codec<FrequencyLookup<FREQ>> trustedCodec;
    @Nullable
    private final Codec<FrequencyLookup<FREQ>> privateCodec;

    private FrequencyController(FrequencyType<FREQ> frequencyType) {
        this.frequencyType = frequencyType;
        Type type = frequencyType.getControllerType();

        Codec<Pair<Optional<UUID>, List<FREQ>>> baseCodec = FrequencyLookup.baseCodec(frequencyType);

        if (type.supportsPublic()) {
            publicCodec = FrequencyLookup.codec(frequencyType, baseCodec, SecurityMode.PUBLIC);
            publicLookup = FrequencyControllerManager.createLookup(frequencyType, null, SecurityMode.PUBLIC, publicCodec);
        } else {
            publicCodec = null;
        }
        if (type.supportsPrivate()) {
            privateLookups = new Object2ObjectOpenHashMap<>();
            privateCodec = FrequencyLookup.codec(frequencyType, baseCodec, SecurityMode.PRIVATE);
        } else {
            privateCodec = null;
        }
        if (type.supportsTrusted()) {
            trustedLookups = new Object2ObjectOpenHashMap<>();
            trustedCodec = FrequencyLookup.codec(frequencyType, baseCodec, SecurityMode.TRUSTED);
        } else {
            trustedCodec = null;
        }
    }

    @Nullable
    private Codec<FrequencyLookup<FREQ>> codecForMode(SecurityMode mode) {
        return switch (mode) {
            case PUBLIC -> publicCodec;
            case PRIVATE -> privateCodec;
            case TRUSTED -> trustedCodec;
        };
    }

    public static <FREQ extends Frequency> FrequencyController<FREQ> create(FrequencyType<FREQ> frequencyType) {
        return new FrequencyController<>(frequencyType);
    }

    @Nullable
    public FrequencyLookup<FREQ> getPublicLookup() {
        if (!frequencyType.getControllerType().supportsPublic()) {
            Mekanism.logger.error("Attempted to access public frequency lookup of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        return publicLookup;
    }

    @Nullable
    public FrequencyLookup<FREQ> getPrivateLookup(@Nullable UUID ownerUUID) {
        SecurityMode securityMode = SecurityMode.PRIVATE;
        return getOrCreateLookup(securityMode, ownerUUID, privateLookups);
    }

    @Nullable
    public FrequencyLookup<FREQ> getTrustedLookup(@Nullable UUID ownerUUID) {
        SecurityMode securityMode = SecurityMode.TRUSTED;
        return getOrCreateLookup(securityMode, ownerUUID, trustedLookups);
    }

    @Nullable
    private FrequencyLookup<FREQ> getOrCreateLookup(SecurityMode securityMode, @Nullable UUID ownerUUID, @Nullable Map<UUID, FrequencyLookup<FREQ>> lookupsByOwner) {
        if (!frequencyType.getControllerType().supports(securityMode) || lookupsByOwner == null) {
            Mekanism.logger.error("Attempted to access {} frequency lookup of type {}. This shouldn't happen!", securityMode.getSerializedName(), frequencyType.getName());
            return null;
        } else if (ownerUUID == null) {
            Mekanism.logger.error("Attempted to access {} frequency lookup of type {} with no owner. This shouldn't happen!", securityMode.getSerializedName(), frequencyType.getName());
            return null;
        }
        FrequencyLookup<FREQ> lookup = lookupsByOwner.get(ownerUUID);
        if (lookup == null) {
            lookup = FrequencyControllerManager.createLookup(frequencyType, ownerUUID, securityMode, codecForMode(securityMode));
            lookupsByOwner.put(ownerUUID, lookup);
        }
        return lookup;
    }

    public Collection<FrequencyLookup<FREQ>> getTrustedLookups() {
        return trustedLookups == null ? Collections.emptyList() : trustedLookups.values();
    }

    public void clear() {
        if (privateLookups != null) {
            privateLookups.clear();
        }
        if (trustedLookups != null) {
            trustedLookups.clear();
        }
    }

    public enum Type {
        PUBLIC_ONLY,
        PRIVATE_ONLY,
        PUBLIC_PRIVATE_TRUSTED;

        public boolean supports(SecurityMode securityMode) {
            return switch (securityMode) {
                case PUBLIC -> this == PUBLIC_ONLY || this == PUBLIC_PRIVATE_TRUSTED;
                case PRIVATE -> this == PRIVATE_ONLY || this == PUBLIC_PRIVATE_TRUSTED;
                case TRUSTED -> this == PUBLIC_PRIVATE_TRUSTED;
            };
        }

        boolean supportsPublic() {
            return supports(SecurityMode.PUBLIC);
        }

        boolean supportsTrusted() {
            return supports(SecurityMode.TRUSTED);
        }

        boolean supportsPrivate() {
            return supports(SecurityMode.PRIVATE);
        }
    }
}