package mekanism.common.component.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.upgrade.Upgrade;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record UpgradeAware(Object2IntMap<Holder<Upgrade>> upgrades, LargeResourceStack<ItemResource> inputSlot, LargeResourceStack<ItemResource> outputSlot) {

    public static final UpgradeAware EMPTY = new UpgradeAware(Object2IntMaps.emptyMap(), LargeResourceStack.ITEM_HELPER.empty(), LargeResourceStack.ITEM_HELPER.empty());

    private static final Codec<Object2IntMap<Holder<Upgrade>>> UPGRADES_CODEC = Codec.unboundedMap(Upgrade.CODEC, ExtraCodecs.POSITIVE_INT).xmap(Object2IntOpenHashMap::new, Function.identity());

    public static final Codec<UpgradeAware> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          UPGRADES_CODEC.fieldOf(SerializationConstants.UPGRADES).forGetter(UpgradeAware::upgrades),
          LargeResourceStack.ITEM_HELPER.orEmptyCodec().fieldOf(SerializationConstants.INPUT).forGetter(UpgradeAware::inputSlot),
          LargeResourceStack.ITEM_HELPER.orEmptyCodec().fieldOf(SerializationConstants.OUTPUT).forGetter(UpgradeAware::outputSlot)
    ).apply(instance, UpgradeAware::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeAware> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.map(_ -> new Object2IntOpenHashMap<>(), Upgrade.STREAM_CODEC, ByteBufCodecs.VAR_INT), UpgradeAware::upgrades,
          LargeResourceStack.ITEM_HELPER.streamCodec(), UpgradeAware::inputSlot,
          LargeResourceStack.ITEM_HELPER.streamCodec(), UpgradeAware::outputSlot,
          UpgradeAware::new
    );

    public UpgradeAware {
        //Make the map unmodifiable to ensure we don't accidentally mutate it
        upgrades = Object2IntMaps.unmodifiable(upgrades);
    }

    public UpgradeAmount getUpgradeCount(ResourceKey<Upgrade> upgrade) {
        //TODO: Do we want to/can we somehow cache this?
        for (ObjectIterator<Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(upgrades); iterator.hasNext(); ) {
            Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
            Holder<Upgrade> holder = entry.getKey();
            if (holder.is(upgrade)) {
                return new UpgradeAmount(entry.getIntValue(), holder.value().max());
            }
        }
        return UpgradeAmount.EMPTY;
    }

    public List<LargeResourceStack<ItemResource>> slotContents() {
        return List.of(inputSlot, outputSlot);
    }

    public record UpgradeAmount(int stored, int max) {

        private static final UpgradeAmount EMPTY = new UpgradeAmount(0, 0);
    }
}