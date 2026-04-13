package mekanism.common.lib.frequency;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import org.jspecify.annotations.Nullable;

public class FrequencyController<FREQ extends Frequency> {

    private final FrequencyType<FREQ> frequencyType;
    private FrequencyLookup<FREQ> publicLookup;
    private Map<UUID, FrequencyLookup<FREQ>> privateLookups;
    private Map<UUID, FrequencyLookup<FREQ>> trustedLookups;

    private final Codec<FrequencyLookup<FREQ>> publicCodec;
    private final Codec<FrequencyLookup<FREQ>> trustedCodec;
    private final Codec<FrequencyLookup<FREQ>> privateCodec;

    private FrequencyController(FrequencyType<FREQ> frequencyType) {
        this.frequencyType = frequencyType;
        Type type = frequencyType.getControllerType();

        Codec<Pair<UUID, List<FREQ>>> baseCodec = FrequencyLookup.baseCodec(frequencyType);

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

    public Codec<FrequencyLookup<FREQ>> codecForMode(SecurityMode mode) {
        return switch (mode) {
            case PUBLIC -> publicCodec;
            case PRIVATE -> privateCodec;
            case TRUSTED -> trustedCodec;
        };
    }

    public static <FREQ extends Frequency> FrequencyController<FREQ> create(FrequencyType<FREQ> frequencyType) {
        return new FrequencyController<>(frequencyType);
    }

    public FrequencyLookup<FREQ> getPublicLookup() {
        if (!frequencyType.getControllerType().supportsPublic()) {
            Mekanism.logger.error("Attempted to access public frequency lookup of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        return publicLookup;
    }

    public FrequencyLookup<FREQ> getPrivateLookup(UUID ownerUUID) {
        SecurityMode securityMode = SecurityMode.PRIVATE;
        return getOrCreateLookup(securityMode, ownerUUID, privateLookups);
    }

    public FrequencyLookup<FREQ> getTrustedLookup(UUID ownerUUID) {
        SecurityMode securityMode = SecurityMode.TRUSTED;
        return getOrCreateLookup(securityMode, ownerUUID, trustedLookups);
    }

    private @Nullable FrequencyLookup<FREQ> getOrCreateLookup(SecurityMode securityMode, UUID ownerUUID, Map<UUID, FrequencyLookup<FREQ>> lookupsByOwner) {
        if (!frequencyType.getControllerType().supports(securityMode)) {
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
        return trustedLookups.values();
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

        boolean supports(SecurityMode securityMode) {
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