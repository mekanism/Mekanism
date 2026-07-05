package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.collection.HashList;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.container.property.ByteArrayPropertyData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import org.jspecify.annotations.Nullable;

public class SyncableStreamCodec<OBJECT extends @Nullable Object> implements ISyncableData {

    public static <FREQUENCY extends @Nullable Frequency> SyncableStreamCodec<FREQUENCY> frequency(FrequencyType<FREQUENCY> type, Supplier<FREQUENCY> getter,
          Consumer<FREQUENCY> setter) {
        return new SyncableStreamCodec<>(new StreamCodec<>() {
            @Override
            public FREQUENCY decode(RegistryFriendlyByteBuf buffer) {
                if (buffer.readBoolean()) {
                    return type.streamCodec().decode(buffer);
                }
                return null;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf output, FREQUENCY value) {
                if (value == null) {
                    output.writeBoolean(false);
                } else {
                    output.writeBoolean(true);
                    type.streamCodec().encode(output, value);
                }
            }
        }, getter, setter);
    }

    public static SyncableStreamCodec<Object2IntMap<Holder<Upgrade>>> upgradeMap(Supplier<Object2IntMap<Holder<Upgrade>>> getter,
          Consumer<Object2IntMap<Holder<Upgrade>>> setter) {
        return new SyncableStreamCodec<>(ByteBufCodecs.map(Object2IntOpenHashMap::new, Upgrade.STREAM_CODEC, ByteBufCodecs.VAR_INT), getter, setter);
    }

    @SuppressWarnings("unchecked")
    public static <FILTER extends IFilter<?>> SyncableStreamCodec<Collection<FILTER>> filterList(Supplier<Collection<FILTER>> getter, Consumer<Collection<FILTER>> setter) {
        return new SyncableStreamCodec<>(((StreamCodec<RegistryFriendlyByteBuf, FILTER>) BaseFilter.GENERIC_STREAM_CODEC).apply(ByteBufCodecs.collection(HashList::new)), getter, setter);
    }

    public static <V> SyncableStreamCodec<List<ResourceKey<V>>> sortedResourceKeyList(ResourceKey<? extends Registry<V>> registry, Supplier<Stream<ResourceKey<V>>> getter,
          Consumer<List<ResourceKey<V>>> setter) {
        return resourceKeyList(registry, () -> getter.get().sorted(Comparator.comparing(ResourceKey::identifier)).toList(), setter);
    }

    public static <V> SyncableStreamCodec<List<ResourceKey<V>>> resourceKeyList(ResourceKey<? extends Registry<V>> registry, Supplier<List<ResourceKey<V>>> getter,
          Consumer<List<ResourceKey<V>>> setter) {
        return new SyncableStreamCodec<>(ResourceKey.streamCodec(registry).apply(ByteBufCodecs.list()), getter, setter);
    }

    private final StreamCodec<? super RegistryFriendlyByteBuf, OBJECT> streamCodec;
    private final Supplier<? extends OBJECT> getter;
    private final Consumer<OBJECT> setter;
    private int lastKnownHashCode;

    protected SyncableStreamCodec(StreamCodec<? super RegistryFriendlyByteBuf, OBJECT> streamCodec, Supplier<? extends OBJECT> getter, Consumer<OBJECT> setter) {
        this.streamCodec = streamCodec;
        this.getter = getter;
        this.setter = setter;
    }

    public OBJECT get() {
        return getter.get();
    }

    public void set(RegistryAccess registryAccess, byte[] rawData) {
        setter.accept(PacketUtils.read(registryAccess, rawData, streamCodec::decode));
    }

    protected int getValueHashCode() {
        OBJECT value = get();
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public DirtyType isDirty() {
        int valuesHashCode = getValueHashCode();
        if (lastKnownHashCode == valuesHashCode) {
            return DirtyType.CLEAN;
        }
        //TODO: Create a way to declare changes that don't affect the entire object at a time? Such as additions/removals from a collection
        // Note that GuiFrequencySelector makes some assumptions based on the fact that this is not currently possible so a new list will occur each time
        lastKnownHashCode = valuesHashCode;
        return DirtyType.DIRTY;
    }

    @Override
    public ByteArrayPropertyData getPropertyData(RegistryAccess registryAccess, short property, DirtyType dirtyType) {
        //Note: We write it to a byte array so that we make sure to effectively copy it (force a serialization and deserialization)
        // whenever we send this as a packet rather than potentially allowing the list to leak from one side to the other in single player
        byte[] rawData = FriendlyByteBufUtil.writeCustomData(buffer -> streamCodec.encode(buffer, get()), registryAccess);
        return new ByteArrayPropertyData(property, rawData);
    }
}