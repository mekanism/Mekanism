package mekanism.common.attachments.containers.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.IAttachedContainers;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
public record AttachedItems(List<ItemStack> containers) implements IAttachedContainers<ItemStack, AttachedItems> {

    public static final AttachedItems EMPTY = new AttachedItems(Collections.emptyList());

    public static final Codec<AttachedItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.LENIENT_OVERSIZED_ITEM_OPTIONAL_CODEC.listOf().fieldOf(SerializationConstants.ITEMS).forGetter(AttachedItems::containers)
    ).apply(instance, AttachedItems::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachedItems> STREAM_CODEC = ItemStack.OPTIONAL_LIST_STREAM_CODEC
          .map(AttachedItems::new, AttachedItems::containers);

    public static AttachedItems create(int containers) {
        return new AttachedItems(NonNullList.withSize(containers, ItemStack.EMPTY));
    }

    public AttachedItems {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        containers = Collections.unmodifiableList(containers);
    }

    @Override
    public ItemStack getEmptyStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public AttachedItems create(List<ItemStack> containers) {
        return new AttachedItems(containers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ItemStack.listMatches(containers, ((AttachedItems) o).containers);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(containers);
    }
}