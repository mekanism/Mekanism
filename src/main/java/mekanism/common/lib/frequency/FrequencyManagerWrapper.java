package mekanism.common.lib.frequency;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.Mekanism;

public class FrequencyManagerWrapper<FREQ extends Frequency> {

    private final Type type;
    private final FrequencyType<FREQ> frequencyType;
    private FrequencyManager<FREQ> publicManager;
    private Map<UUID, FrequencyManager<FREQ>> privateManagers;

    private FrequencyManagerWrapper(Type type, FrequencyType<FREQ> frequencyType) {
        this.type = type;
        this.frequencyType = frequencyType;

        if (type.supportsPublic()) {
            publicManager = new FrequencyManager<>(frequencyType);
        }
        if (type.supportsPrivate()) {
            privateManagers = new Object2ObjectOpenHashMap<>();
        }
    }

    public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> create(FrequencyType<FREQ> frequencyType, Type type) {
        return new FrequencyManagerWrapper<>(type, frequencyType);
    }

    public FrequencyManager<FREQ> getPublicManager() {
        if (!type.supportsPublic()) {
            Mekanism.logger.error("Attempted to access public frequency manager of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        return publicManager;
    }

    public FrequencyManager<FREQ> getPrivateManager(UUID ownerUUID) {
        if (!type.supportsPrivate()) {
            Mekanism.logger.error("Attempted to access private frequency manager of type {}. This shouldn't happen!", frequencyType.getName());
            return null;
        } else if (ownerUUID == null) {
            Mekanism.logger.error("Attempted to access private frequency manager of type {} with no owner. This shouldn't happen!", frequencyType.getName());
            return null;
        }

        if (!privateManagers.containsKey(ownerUUID)) {
            FrequencyManager<FREQ> manager = new FrequencyManager<>(frequencyType, ownerUUID);
            privateManagers.put(ownerUUID, manager);
            manager.createOrLoad();
        }
        return privateManagers.get(ownerUUID);
    }

    public void clear() {
        if (privateManagers != null) {
            privateManagers.clear();
        }
    }

    public enum Type {
        PUBLIC_ONLY,
        PRIVATE_ONLY,
        PUBLIC_PRIVATE;

        boolean supportsPublic() {
            return this == PUBLIC_ONLY || this == PUBLIC_PRIVATE;
        }

        boolean supportsPrivate() {
            return this == PRIVATE_ONLY || this == PUBLIC_PRIVATE;
        }
    }
}