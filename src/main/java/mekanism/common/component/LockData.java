package mekanism.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

@NothingNullByDefault
public record LockData(ItemResource lock) {

    public static final LockData EMPTY = new LockData(ItemResource.EMPTY);

    public static final Codec<LockData> CODEC = RecordCodecBuilder.<LockData>create(instance -> instance.group(
          ItemResource.OPTIONAL_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(LockData::lock)
    ).apply(instance, LockData::create)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored lock data: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LockData> STREAM_CODEC = ItemResource.STREAM_CODEC.map(LockData::create, LockData::lock);

    public static LockData create(ItemResource lock) {
        if (lock.isEmpty()) {
            return EMPTY;
        }
        return new LockData(lock);
    }
}