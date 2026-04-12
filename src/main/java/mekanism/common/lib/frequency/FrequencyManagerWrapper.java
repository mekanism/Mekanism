package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;

public class FrequencyManagerWrapper<FREQ extends Frequency> {

    private final Type type;
    private final FrequencyType<FREQ> frequencyType;
    private FrequencyLookup<FREQ> publicManager;
    private Map<UUID, FrequencyLookup<FREQ>> privateManagers;
    private Map<UUID, FrequencyLookup<FREQ>> trustedManagers;

    private FrequencyManagerWrapper(Type type, FrequencyType<FREQ> frequencyType) {
        this.type = type;
        this.frequencyType = frequencyType;

        if (type.supportsPublic()) {
            publicManager = new FrequencyLookup<>(frequencyType);
        }
        if (type.supportsPrivate()) {
            privateManagers = new Object2ObjectOpenHashMap<>();
        }
        if (type.supportsTrusted()) {
            trustedManagers = new Object2ObjectOpenHashMap<>();
        }
    }

    public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> create(FrequencyType<FREQ> frequencyType, Type type) {
        return new FrequencyManagerWrapper<>(type, frequencyType);
    }

    public FrequencyLookup<FREQ> getPublicManager() {
        if (!type.supportsPublic()) {
            Mekanism.logger.error("Attempted to access public frequency manager of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        return publicManager;
    }

    public FrequencyLookup<FREQ> getPrivateManager(UUID ownerUUID) {
        if (!type.supportsPrivate()) {
            Mekanism.logger.error("Attempted to access private frequency manager of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        } else if (ownerUUID == null) {
            Mekanism.logger.error("Attempted to access private frequency manager of type {} with no owner. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        FrequencyLookup<FREQ> manager = privateManagers.get(ownerUUID);
        if (manager == null) {
            manager = new FrequencyLookup<>(frequencyType, ownerUUID, SecurityMode.PRIVATE);
            manager.createOrLoad();
            privateManagers.put(ownerUUID, manager);
        }
        return manager;
    }

    public FrequencyLookup<FREQ> getTrustedManager(UUID ownerUUID) {
        if (!type.supportsTrusted()) {
            Mekanism.logger.error("Attempted to access trusted frequency manager of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        } else if (ownerUUID == null) {
            Mekanism.logger.error("Attempted to access trusted frequency manager of type {} with no owner. This shouldn't happen!", frequencyType.getName());
            return null;
        }
        FrequencyLookup<FREQ> manager = trustedManagers.get(ownerUUID);
        if (manager == null) {
            manager = new FrequencyLookup<>(frequencyType, ownerUUID, SecurityMode.TRUSTED);
            manager.createOrLoad();
            trustedManagers.put(ownerUUID, manager);
        }
        return manager;
    }

    public Collection<FrequencyLookup<FREQ>> getTrustedManagers() {
        return trustedManagers.values();
    }

    public void clear() {
        if (privateManagers != null) {
            privateManagers.clear();
        }
        if (trustedManagers != null) {
            trustedManagers.clear();
        }
    }

    public enum Type {
        PUBLIC_ONLY,
        PRIVATE_ONLY,
        PUBLIC_PRIVATE_TRUSTED;

        boolean supportsPublic() {
            return this == PUBLIC_ONLY || this == PUBLIC_PRIVATE_TRUSTED;
        }

        boolean supportsTrusted() {
            return this == PUBLIC_PRIVATE_TRUSTED;
        }

        boolean supportsPrivate() {
            return this == PRIVATE_ONLY || this == PUBLIC_PRIVATE_TRUSTED;
        }
    }
}