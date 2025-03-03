package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * @param overflow Note: Sorted map to ensure each call to save is in the same order so that there is more uniformity
 */
@NothingNullByDefault
public record OverflowAware(Object2IntSortedMap<HashedItem> overflow) {

    public static final OverflowAware EMPTY = new OverflowAware(Object2IntSortedMaps.emptyMap());

    public static final Codec<OverflowAware> CODEC = ItemCount.CODEC.listOf()
          .promotePartial(error -> Mekanism.logger.error("Failed to load overflown items: {}", error)).xmap(
                counts -> {
                    Object2IntSortedMap<HashedItem> overflow = new Object2IntLinkedOpenHashMap<>(counts.size());
                    for (ItemCount itemCount : counts) {
                        overflow.mergeInt(itemCount.type(), itemCount.count(), Integer::sum);
                    }
                    return new OverflowAware(overflow);
                }, overflowAware -> {
                    List<ItemCount> counts = new ArrayList<>(overflowAware.overflow().size());
                    for (ObjectIterator<Entry<HashedItem>> iterator = Object2IntMaps.fastIterator(overflowAware.overflow()); iterator.hasNext(); ) {
                        Object2IntMap.Entry<HashedItem> entry = iterator.next();
                        counts.add(new ItemCount(entry.getKey(), entry.getIntValue()));
                    }
                    return counts;
                });
    public static final StreamCodec<RegistryFriendlyByteBuf, OverflowAware> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, HashedItem, Integer, Object2IntSortedMap<HashedItem>>map(
          Object2IntLinkedOpenHashMap::new, HashedItem.STREAM_CODEC, ByteBufCodecs.VAR_INT
    ).map(OverflowAware::new, OverflowAware::overflow);

    public OverflowAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        overflow = Object2IntSortedMaps.unmodifiable(overflow);
    }

    private record ItemCount(HashedItem type, int count) {

        public static final Codec<ItemCount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              HashedItem.CODEC.fieldOf(SerializationConstants.TYPE).forGetter(ItemCount::type),
              ExtraCodecs.POSITIVE_INT.fieldOf(SerializationConstants.COUNT).forGetter(ItemCount::count)
        ).apply(instance, ItemCount::new));
    }
}