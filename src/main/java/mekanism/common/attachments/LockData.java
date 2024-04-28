package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record LockData(ItemStack lock) {

    public static final LockData EMPTY = new LockData(ItemStack.EMPTY);

    public static final Codec<LockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          ItemStack.CODEC.fieldOf(JsonConstants.OUTPUT).forGetter(LockData::lock)
    ).apply(instance, LockData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LockData> STREAM_CODEC = ItemStack.STREAM_CODEC.map(LockData::new, LockData::lock);

    public LockData {
        lock = lock.copy();
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