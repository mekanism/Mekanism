package mekanism.common.lib;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Map;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Version of array map which does a proper BiConsumer and a couple other microoptimisations.
 * Used in {@link mekanism.common.lib.frequency.TileComponentFrequency}, which is in a very hot path as EVERY mek machine uses one
 */
public class CustomObjectToObjectArrayMap<KEY, VALUE> extends Object2ObjectArrayMap<KEY, VALUE> {

    public CustomObjectToObjectArrayMap() {
    }

    public CustomObjectToObjectArrayMap(Map<? extends KEY, ? extends VALUE> m) {
        super(m);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(BiConsumer<? super KEY, ? super VALUE> consumer) {
        if (size == 0) {
            return;
        }
        final int max = size;
        for (int i = 0; i < max; i++) {
            consumer.accept((KEY)key[i], (VALUE)value[i]);
        }
    }

    //save a tiny bit of heap space and not create an object
    @Override
    public @NotNull ObjectSet<KEY> keySet() {
        return size == 0 ? ObjectSets.emptySet() : super.keySet();
    }

    //save a tiny bit of heap space and not create an object
    @Override
    public @NotNull ObjectSet<Map.Entry<KEY, VALUE>> entrySet() {
        return size == 0 ? ObjectSets.emptySet() : super.entrySet();
    }
}
