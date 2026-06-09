package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand, int extra) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketItemGuiInteract> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("item_gui_interact"));
    public static final StreamCodec<ByteBuf, PacketItemGuiInteract> STREAM_CODEC = StreamCodec.composite(
          ItemGuiInteraction.STREAM_CODEC, PacketItemGuiInteract::interaction,
          InteractionHand.STREAM_CODEC, PacketItemGuiInteract::hand,
          ByteBufCodecs.VAR_INT, PacketItemGuiInteract::extra,
          PacketItemGuiInteract::new
    );

    public PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand) {
        this(interaction, hand, 0);
    }

    @Override
    public CustomPacketPayload.Type<PacketItemGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        if (itemAccess.getAmount() > 0) {
            interaction.consume(itemAccess, player, extra);
        }
    }

    public enum ItemGuiInteraction {
        TARGET_DIRECTION_BUTTON((itemAccess, _, _) -> {
            ItemResource resource = itemAccess.getResource();
            boolean currentValue = resource.getOrDefault(MekanismDataComponents.INSERT_INTO_FREQUENCY, true);
            ItemAccessUtils.exchange(itemAccess, resource.with(MekanismDataComponents.INSERT_INTO_FREQUENCY, !currentValue), null);
        }),

        NEXT_SECURITY_MODE((itemAccess, player, _) -> SecurityUtils.get().incrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(itemAccess), null)),
        PREVIOUS_SECURITY_MODE((itemAccess, player, _) -> SecurityUtils.get().decrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(itemAccess), null));

        public static final IntFunction<ItemGuiInteraction> BY_ID = ByIdMap.continuous(ItemGuiInteraction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ItemGuiInteraction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ItemGuiInteraction::ordinal);

        private final ConsumerForItem consumerForItem;

        ItemGuiInteraction(ConsumerForItem consumerForItem) {
            this.consumerForItem = consumerForItem;
        }

        public void consume(ItemAccess itemAccess, Player player, int extra) {
            consumerForItem.accept(itemAccess, player, extra);
        }

        @FunctionalInterface
        private interface ConsumerForItem {

            void accept(ItemAccess itemAccess, Player player, int extra);
        }
    }
}