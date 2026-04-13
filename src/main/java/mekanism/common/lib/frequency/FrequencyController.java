package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;

public class FrequencyController<FREQ extends Frequency> {

    private final Type type;
    private final FrequencyType<FREQ> frequencyType;
    private FrequencyLookup<FREQ> publicLookup;
    private Map<UUID, FrequencyLookup<FREQ>> privateLookups;
    private Map<UUID, FrequencyLookup<FREQ>> trustedLookups;

    private FrequencyController(Type type, FrequencyType<FREQ> frequencyType) {
        this.type = type;
        this.frequencyType = frequencyType;

        if (type.supportsPublic()) {
            publicLookup = new FrequencyLookup<>(frequencyType);
            FrequencyTypes.registerTickable(publicLookup);
        }
        if (type.supportsPrivate()) {
            privateLookups = new Object2ObjectOpenHashMap<>();
        }
        if (type.supportsTrusted()) {
            trustedLookups = new Object2ObjectOpenHashMap<>();
        }
    }

    public static <FREQ extends Frequency> FrequencyController<FREQ> create(FrequencyType<FREQ> frequencyType, Type type) {
        return new FrequencyController<>(type, frequencyType);
    }

    public FrequencyLookup<FREQ> getPublicLookup() {
        if (!type.supportsPublic()) {
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
        if (!type.supports(securityMode)) {
            Mekanism.logger.error("Attempted to access {} frequency lookup of type {}. This shouldn't happen!", securityMode.getSerializedName(), frequencyType.getName());
            return null;
        } else if (ownerUUID == null) {
            Mekanism.logger.error("Attempted to access {} frequency lookup of type {} with no owner. This shouldn't happen!", securityMode.getSerializedName(), frequencyType.getName());
            return null;
        }
        FrequencyLookup<FREQ> lookup = lookupsByOwner.get(ownerUUID);
        if (lookup == null) {
            lookup = new FrequencyLookup<>(frequencyType, ownerUUID, securityMode);
            FrequencyTypes.registerTickable(lookup);
            lookup.createOrLoad();
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