package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record LockData(ItemStack lock) {

    public static final LockData EMPTY = new LockData(ItemStack.EMPTY);

    public static final Codec<LockData> CODEC = RecordCodecBuilder.<LockData>create(instance -> instance.group(
          ItemStack.SINGLE_ITEM_CODEC.fieldOf(SerializationConstants.OUTPUT).forGetter(LockData::lock)
    ).apply(instance, LockData::new)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored lock data: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LockData> STREAM_CODEC = ItemStack.STREAM_CODEC.map(LockData::new, LockData::lock);

    public LockData {
        lock = lock.copy();
    }

    public static LockData create(ItemStack lock) {
        if (lock.isEmpty()) {
            return EMPTY;
        }
        return new LockData(lock.copyWithCount(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ItemStack.matches(lock, ((LockData) o).lock);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(lock);
    }
}