package mekanism.common.network.to_client.container.property.resource;

import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ItemResourcePropertyData extends ResourcePropertyData<ItemResource> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResourcePropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ItemResource.STREAM_CODEC, ResourcePropertyData::getValue,
          ItemResourcePropertyData::new
    );

    public ItemResourcePropertyData(short property, ItemResource value) {
        super(PropertyType.ITEM_TYPE, property, value);
    }
}