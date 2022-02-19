package mekanism.common.base;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;

public class KeySync {

    public static final int ASCEND = 0;
    public static final int BOOST = 1;

    public final Map<UUID, KeySet> keys = new Object2ObjectOpenHashMap<>();

    public KeySet getPlayerKeys(UUID playerUUID) {
        return keys.get(playerUUID);
    }

    public void add(UUID playerUUID, int key) {
        if (keys.containsKey(playerUUID)) {
            keys.get(playerUUID).keysActive.add(key);
        } else {
            keys.put(playerUUID, new KeySet(key));
        }
    }

    public void remove(UUID playerUUID, int key) {
        if (keys.containsKey(playerUUID)) {
            keys.get(playerUUID).keysActive.remove(key);
        }
    }

    public boolean has(UUID playerUUID, int key) {
        return keys.containsKey(playerUUID) && keys.get(playerUUID).keysActive.contains(key);
    }

    public void update(UUID playerUUID, int key, boolean add) {
        if (add) {
            add(playerUUID, key);
        } else {
            remove(playerUUID, key);
        }
    }

    public static class KeySet {

        public final IntSet keysActive = new IntOpenHashSet();

        public KeySet(int key) {
            keysActive.add(key);
        }
    }
}