package mekanism.common.network.to_client.qio;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem.UUIDAwareHashedItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketBatchItemViewerSync extends PacketQIOItemViewerGuiSync {

    public static final CustomPacketPayload.Type<PacketBatchItemViewerSync> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("batch_qio_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketBatchItemViewerSync> STREAM_CODEC = streamCodec(PacketBatchItemViewerSync::new);

    public PacketBatchItemViewerSync(long countCapacity, int typeCapacity, Object2LongMap<UUIDAwareHashedItem> itemMap) {
        super(countCapacity, typeCapacity, itemMap);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketBatchItemViewerSync> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (context.player().containerMenu instanceof QIOItemViewerContainer container) {
            container.handleBatchUpdate(itemMap, countCapacity, typeCapacity);
        }
    }
}