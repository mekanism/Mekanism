package mekanism.common.attachments.qio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.SerializerHelper;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record PortableDashboardContents(List<ItemStack> contents) {

    public static final int TOTAL_SLOTS = (3 * 3) * IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS;

    //TODO: Do we want to try and make this an empty list? It not being empty means it is easier to not serialize things when the windows become empty
    public static final PortableDashboardContents EMPTY = new PortableDashboardContents(NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY));

    public static final Codec<PortableDashboardContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          SerializerHelper.LENIENT_OPTIONAL_STACK_CODEC.listOf(TOTAL_SLOTS, TOTAL_SLOTS).fieldOf(SerializationConstants.ITEMS).forGetter(PortableDashboardContents::contents)
    ).apply(instance, PortableDashboardContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDashboardContents> STREAM_CODEC = ItemStack.OPTIONAL_LIST_STREAM_CODEC
          .map(PortableDashboardContents::new, PortableDashboardContents::contents);

    public PortableDashboardContents {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        contents = Collections.unmodifiableList(contents);
    }

    public PortableDashboardContents with(int window, int index, ItemStack stack) {
        List<ItemStack> copy = new ArrayList<>(contents);
        copy.set(getIndex(window, index), stack);
        return new PortableDashboardContents(copy);
    }

    public ItemStack getSlotContents(int window, int index) {
        return contents.get(getIndex(window, index)).copy();
    }

    private static int getIndex(int window, int index) {
        return 9 * window + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ItemStack.listMatches(contents, ((PortableDashboardContents) o).contents);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashStackList(contents);
    }
}