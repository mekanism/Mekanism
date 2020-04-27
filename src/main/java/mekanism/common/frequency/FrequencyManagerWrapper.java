package mekanism.common.frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.security.ISecurityTile;
import net.minecraft.tileentity.TileEntity;

public class FrequencyManagerWrapper<FREQ extends Frequency> {

    private Type type;
    private FrequencyType<FREQ> frequencyType;
    private FrequencyManager<FREQ> publicManager;
    private Map<UUID, FrequencyManager<FREQ>> privateManagers;

    private FrequencyManagerWrapper(Type type, FrequencyType<FREQ> frequencyType, FrequencyManager<FREQ> publicManager, Map<UUID, FrequencyManager<FREQ>> privateManagers) {
        this.frequencyType = frequencyType;
        this.publicManager = publicManager;
        this.privateManagers = privateManagers;
    }

    public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> createPublicOnly(FrequencyType<FREQ> type) {
        return new FrequencyManagerWrapper<>(Type.PUBLIC_ONLY, type, new FrequencyManager<>(type), null);
    }

    public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> createPrivateOnly(FrequencyType<FREQ> type) {
        return new FrequencyManagerWrapper<>(Type.PRIVATE_ONLY, type, null, new Object2ObjectOpenHashMap<>());
    }

    public static <FREQ extends Frequency> FrequencyManagerWrapper<FREQ> createPublicPrivate(FrequencyType<FREQ> type) {
        return new FrequencyManagerWrapper<>(Type.PUBLIC_PRIVATE, type, new FrequencyManager<>(type), new Object2ObjectOpenHashMap<>());
    }

    public FrequencyManager<FREQ> getPublicManager() {
        return publicManager;
    }

    public FrequencyManager<FREQ> getPrivateManager(UUID ownerUUID) {
        if (!privateManagers.containsKey(ownerUUID)) {
            FrequencyManager<FREQ> manager = new FrequencyManager<>(frequencyType, ownerUUID);
            privateManagers.put(ownerUUID, manager);
            manager.createOrLoad();
        }
        return privateManagers.get(ownerUUID);
    }

    public FrequencyManager<FREQ> getManager(FREQ freq) {
        return freq.isPrivate() ? getPrivateManager(freq.ownerUUID) : getPublicManager();
    }

    public List<FREQ> getPublicFrequencies(TileEntity tile, List<FREQ> cache) {
        return tile.getWorld().isRemote() ? cache : new ArrayList<>(getPublicManager().getFrequencies().values());
    }

    public <TILE extends TileEntity & ISecurityTile> List<FREQ> getPrivateFrequencies(TILE tile, List<FREQ> cache) {
        if (tile.getWorld().isRemote()) {
            return cache;
        }
        UUID ownerUUID = tile.getSecurity().getOwnerUUID();
        if (ownerUUID == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(getPrivateManager(ownerUUID).getFrequencies().values());
    }

    public void clear() {
        if (privateManagers != null)
            privateManagers.clear();
    }

    private static enum Type {
        PUBLIC_ONLY,
        PRIVATE_ONLY,
        PUBLIC_PRIVATE;
    }
}
