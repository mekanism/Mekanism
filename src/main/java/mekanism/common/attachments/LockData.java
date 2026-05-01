package mekanism.common.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.ItemStackTemplateHelper;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

//TODO - 26.1: Replace this with ItemResource, and re-evaluate all other data components that use ItemStacks and either transfere them to ItemResource or ItemStackTemplate
@NothingNullByDefault
public record LockData(@Nullable ItemStackTemplate lock) {

    public static final LockData EMPTY = new LockData(null);

    public static final Codec<LockData> CODEC = RecordCodecBuilder.<LockData>create(instance -> instance.group(
          ItemStackTemplateHelper.NO_COUNT_CODEC.optionalFieldOf(SerializationConstants.OUTPUT).forGetter(LockData::toCodec)
    ).apply(instance, LockData::fromCodec)).orElse(
          (Consumer<String>) error -> Mekanism.logger.error("Failed to load stored lock data: {}", error),
          EMPTY
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LockData> STREAM_CODEC = ItemStackTemplateHelper.OPTIONAL_STREAM_CODEC.map(LockData::fromCodec, LockData::toCodec);

    public static LockData create(ItemStack lock) {
        if (lock.isEmpty()) {
            return EMPTY;
        }
        return new LockData(new ItemStackTemplate(lock.typeHolder(), lock.getComponentsPatch()));
    }

    public static LockData create(@Nullable ItemStackTemplate lock) {
        if (lock == null) {
            return EMPTY;
        }
        return new LockData(lock);//reuse as they're immutable
    }

    static LockData fromCodec(Optional<ItemStackTemplate> template) {
        return template.isPresent() ? new LockData(template.get()) : EMPTY;
    }

    Optional<ItemStackTemplate> toCodec() {
        return Optional.ofNullable(lock);
    }

    public ItemStack asItemStack() {
        return lock == null ? ItemStack.EMPTY : lock.create();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ItemStackTemplateHelper.matches(lock, ((LockData) o).lock);
    }

    @Override
    public int hashCode() {
        return ItemStackTemplateHelper.hashItemAndComponents(lock);
    }
}