package mekanism.common.inventory.container.sync.map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.upgrade.Upgrade;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/// Version of [net.minecraft.world.inventory.DataSlot] for handling registry entry based lists
public class SyncableUpgradeMap extends SyncableMap<Holder<Upgrade>, Integer, Object2IntMap<Holder<Upgrade>>> {

    private static final StreamCodec<RegistryFriendlyByteBuf, Object2IntMap<Holder<Upgrade>>> STREAM_CODEC = ByteBufCodecs.map(Object2IntOpenHashMap::new,
          Upgrade.STREAM_CODEC,
          ByteBufCodecs.VAR_INT
    );

    public SyncableUpgradeMap(Supplier<Object2IntMap<Holder<Upgrade>>> getter, Consumer<Object2IntMap<Holder<Upgrade>>> setter) {
        super(getter, setter);
    }

    @Override
    protected StreamCodec<RegistryFriendlyByteBuf, Object2IntMap<Holder<Upgrade>>> streamCodec() {
        return STREAM_CODEC;
    }
}