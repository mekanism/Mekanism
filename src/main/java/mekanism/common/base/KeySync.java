package mekanism.common.base;

import java.util.Map;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;

public class KeySync {

    public static int ASCEND = 0;
    public static int DESCEND = 1;
    public static int BOOST = 2;

    public Map<PlayerEntity, KeySet> keys = new Object2ObjectOpenHashMap<>();

    public KeySet getPlayerKeys(PlayerEntity player) {
        return keys.get(player);
    }

    public void add(PlayerEntity player, int key) {
        if (!keys.containsKey(player)) {
            keys.put(player, new KeySet(key));
            return;
        }
        keys.get(player).keysActive.add(key);
    }

    public void remove(PlayerEntity player, int key) {
        if (!keys.containsKey(player)) {
            return;
        }
        keys.get(player).keysActive.remove(key);
    }

    public boolean has(PlayerEntity player, int key) {
        if (!keys.containsKey(player)) {
            return false;
        }
        return keys.get(player).keysActive.contains(key);
    }

    public void update(PlayerEntity player, int key, boolean add) {
        if (add) {
            add(player, key);
        } else {
            remove(player, key);
        }
    }

    public static class KeySet {

        public IntSet keysActive = new IntOpenHashSet();

        public KeySet(int key) {
            keysActive.add(key);
        }
    }
}