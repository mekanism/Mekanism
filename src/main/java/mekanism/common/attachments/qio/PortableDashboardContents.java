package mekanism.common.attachments.qio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.SerializationConstants;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

//Note: While technically we could use an ItemStack or ItemStackTemplate for the list of contents, we use the custom large resource stack as it is immutable
// and then if we do decide to make it so that the dashboard can hold long amount of items, then it will natively support it
public record PortableDashboardContents(List<LargeResourceStack<ItemResource>> contents) {//TODO - 26.1: Re-evaluate this decision ^

    public static final int TOTAL_SLOTS = 9 * IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS;

    //TODO: Do we want to try and make this an empty list? It not being empty means it is easier to not serialize things when the windows become empty
    public static final PortableDashboardContents EMPTY = new PortableDashboardContents(NonNullList.withSize(TOTAL_SLOTS, LargeResourceStack.ITEM_HELPER.empty()));

    public static final Codec<PortableDashboardContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          LargeResourceStack.ITEM_HELPER.orEmptyCodec().listOf(TOTAL_SLOTS, TOTAL_SLOTS).fieldOf(SerializationConstants.ITEMS).forGetter(PortableDashboardContents::contents)
    ).apply(instance, PortableDashboardContents::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PortableDashboardContents> STREAM_CODEC =
          ByteBufCodecs.<RegistryFriendlyByteBuf, LargeResourceStack<ItemResource>, List<LargeResourceStack<ItemResource>>>collection(
                NonNullList::createWithCapacity, LargeResourceStack.ITEM_HELPER.streamCodec()
          ).map(PortableDashboardContents::new, PortableDashboardContents::contents);

    public PortableDashboardContents {
        //Make the list unmodifiable to ensure we don't accidentally mutate it
        contents = Collections.unmodifiableList(contents);
    }

    public PortableDashboardContents with(int window, int index, ItemResource itemType, long amount) {
        return with(window, index, LargeResourceStack.ITEM_HELPER.createStack(itemType, amount));
    }

    public PortableDashboardContents with(int window, int index, LargeResourceStack<ItemResource> stack) {
        List<LargeResourceStack<ItemResource>> copy = new ArrayList<>(contents);
        copy.set(getIndex(window, index), stack);
        return new PortableDashboardContents(copy);
    }

    public LargeResourceStack<ItemResource> getSlotContents(int window, int index) {
        return contents.get(getIndex(window, index));
    }

    private static int getIndex(int window, int index) {
        return 9 * window + index;
    }
}