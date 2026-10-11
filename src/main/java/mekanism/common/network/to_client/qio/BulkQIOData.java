package mekanism.common.network.to_client.qio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.QIOFrequency.QIOItemTypeData;
import mekanism.common.inventory.ISlotClickHandler.IScrollableSlot;
import mekanism.common.inventory.container.QIOItemViewerContainer.ItemSlotData;
import mekanism.common.lib.inventory.UUIDItemResource;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public record BulkQIOData(Map<UUID, ItemSlotData> inventory, long countCapacity, int typeCapacity, long totalItems, List<IScrollableSlot> items) {

    public static final BulkQIOData INITIAL_SERVER = new BulkQIOData(Collections.emptyMap(), 0, 0, 0, Collections.emptyList());

    public static void encodeToPacket(RegistryFriendlyByteBuf buffer, @Nullable QIOFrequency frequency) {
        buffer.writeBoolean(frequency != null);
        if (frequency != null) {
            Map<ItemResource, QIOItemTypeData> itemDataMap = frequency.getItemDataMap();
            //Manual implementation of encoding PacketUpdateItemViewer.ITEM_MAP_CODEC without having to actually create the intermediary UUIDItemResource instance
            ByteBufCodecs.VAR_INT.encode(buffer, itemDataMap.size());
            for (QIOItemTypeData data : itemDataMap.values()) {
                //The following two lines are equivalent to encoding UUIDItemResource.STREAM_CODEC
                UUIDUtil.STREAM_CODEC.encode(buffer, data.getItemUUID());
                ItemResource.STREAM_CODEC.encode(buffer, data.getItemType());
                ByteBufCodecs.VAR_LONG.encode(buffer, data.getCount());
            }
            //End implementation of encoding ITEM_MAP_CODEC
            ByteBufCodecs.VAR_LONG.encode(buffer, frequency.getTotalItemCountCapacity());
            ByteBufCodecs.VAR_INT.encode(buffer, frequency.getTotalItemTypeCapacity());
        }
    }

    public static BulkQIOData fromPacket(RegistryFriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            long totalItems = 0;
            //Note: We manually handle decoding the map so that we can avoid having to create an intermediary holding map
            int itemMapSize = ByteBufCodecs.VAR_INT.decode(buffer);
            //Note: Use a ReferenceArrayList to allow for instance equality checking when removing elements
            List<IScrollableSlot> itemList = new ReferenceArrayList<>(itemMapSize);
            Map<UUID, ItemSlotData> inventory = new Object2ObjectOpenHashMap<>(itemMapSize);
            for (int i = 0; i < itemMapSize; i++) {
                ItemSlotData slotData = new ItemSlotData(UUIDItemResource.STREAM_CODEC.decode(buffer), ByteBufCodecs.VAR_LONG.decode(buffer), buffer.registryAccess());
                totalItems += slotData.count();
                itemList.add(slotData);
                inventory.put(slotData.itemUUID(), slotData);
            }
            return new BulkQIOData(inventory, ByteBufCodecs.VAR_LONG.decode(buffer), ByteBufCodecs.VAR_INT.decode(buffer), totalItems, itemList);
        }
        //If we don't have a frequency, just use empty uninitialized data that is modifiable
        //TODO: Do we even need to be using a modifiable version, instead of say INITIAL_SERVER?
        // Theoretically because of how things are done, if the frequency changes we get a new open container packet
        // with the bulk data in it, and if there is no frequency we shouldn't get any update contents packets
        return new BulkQIOData(new Object2ObjectOpenHashMap<>(), 0, 0, 0, new ReferenceArrayList<>());
    }
}