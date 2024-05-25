package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMaps;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.inventory.HashedItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

/**
 * @param overflow Note: Sorted map to ensure each call to save is in the same order so that there is more uniformity
 */
@NothingNullByDefault
public record OverflowAware(Object2IntSortedMap<HashedItem> overflow) {

    public static final OverflowAware EMPTY = new OverflowAware(Object2IntSortedMaps.emptyMap());

    //TODO - 1.20.5: I don't think this is a consistent order anymore?
    public static final Codec<OverflowAware> CODEC = Codec.unboundedMap(HashedItem.CODEC, ExtraCodecs.POSITIVE_INT)
          .xmap(map -> new OverflowAware(new Object2IntLinkedOpenHashMap<>(map)), OverflowAware::overflow);
    public static final StreamCodec<RegistryFriendlyByteBuf, OverflowAware> STREAM_CODEC = ByteBufCodecs.<RegistryFriendlyByteBuf, HashedItem, Integer, Object2IntSortedMap<HashedItem>>map(
          Object2IntLinkedOpenHashMap::new, HashedItem.STREAM_CODEC, ByteBufCodecs.VAR_INT
    ).map(OverflowAware::new, OverflowAware::overflow);

    public OverflowAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        overflow = Object2IntSortedMaps.unmodifiable(overflow);
    }

    public static ListTag writeOverflow(HolderLookup.Provider provider, Object2IntMap<HashedItem> overflow) {
        ListTag overflowTag = new ListTag();
        for (Object2IntMap.Entry<HashedItem> entry : overflow.object2IntEntrySet()) {
            CompoundTag overflowComponent = new CompoundTag();
            overflowComponent.put(SerializationConstants.TYPE, entry.getKey().internalToNBT(provider));
            overflowComponent.putInt(SerializationConstants.COUNT, entry.getIntValue());
            overflowTag.add(overflowComponent);
        }
        return overflowTag;
    }

    public static void readOverflow(HolderLookup.Provider provider, Object2IntMap<HashedItem> overflow, ListTag overflowTag) {
        for (int i = 0, size = overflowTag.size(); i < size; i++) {
            CompoundTag overflowComponent = overflowTag.getCompound(i);
            int count = overflowComponent.getInt(SerializationConstants.COUNT);
            if (count > 0) {
                //The count should always be greater than zero, but validate it just in case before trying to read the item
                ItemStack s = ItemStack.parseOptional(provider, overflowComponent.getCompound(SerializationConstants.TYPE));
                //Only add the item if the item could be read. If it can't that means the mod adding the item was probably removed
                if (!s.isEmpty()) {
                    //Note: We can use a raw stack as we just created a new stack from NBT
                    overflow.put(HashedItem.raw(s), count);
                }
            }
        }
    }
}