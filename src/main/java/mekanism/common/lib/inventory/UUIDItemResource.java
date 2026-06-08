package mekanism.common.lib.inventory;

import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record UUIDItemResource(UUID uuid, ItemResource itemType) {

    //TODO: Eventually we might want to make it so that we only need to sync the hashed item for types we haven't sent a given client yet so that then
    // we can also send a smaller packet to each client until they disconnect and then we clear what packets they know
    public static final StreamCodec<RegistryFriendlyByteBuf, UUIDItemResource> STREAM_CODEC = StreamCodec.composite(
          UUIDUtil.STREAM_CODEC, UUIDItemResource::uuid,
          ItemResource.STREAM_CODEC, UUIDItemResource::itemType,
          UUIDItemResource::new
    );
}