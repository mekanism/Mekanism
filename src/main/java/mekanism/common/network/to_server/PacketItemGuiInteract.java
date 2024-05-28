package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand, int extra) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketItemGuiInteract> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("item_gui_interact"));
    public static final StreamCodec<ByteBuf, PacketItemGuiInteract> STREAM_CODEC = StreamCodec.composite(
          ItemGuiInteraction.STREAM_CODEC, PacketItemGuiInteract::interaction,
          PacketUtils.INTERACTION_HAND_STREAM_CODEC, PacketItemGuiInteract::hand,
          ByteBufCodecs.VAR_INT, PacketItemGuiInteract::extra,
          PacketItemGuiInteract::new
    );

    public PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand) {
        this(interaction, hand, 0);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketItemGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            interaction.consume(stack, player, extra);
        }
    }

    public enum ItemGuiInteraction {
        TARGET_DIRECTION_BUTTON((stack, player, extra) -> stack.update(MekanismDataComponents.INSERT_INTO_FREQUENCY, true, val -> !val)),

        NEXT_SECURITY_MODE((stack, player, extra) -> SecurityUtils.get().incrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack))),
        PREVIOUS_SECURITY_MODE((stack, player, extra) -> SecurityUtils.get().decrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack)));

        public static final IntFunction<ItemGuiInteraction> BY_ID = ByIdMap.continuous(ItemGuiInteraction::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ItemGuiInteraction> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ItemGuiInteraction::ordinal);

        private final TriConsumer<ItemStack, Player, Integer> consumerForTile;

        ItemGuiInteraction(TriConsumer<ItemStack, Player, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(ItemStack stack, Player player, int extra) {
            consumerForTile.accept(stack, player, extra);
        }
    }
}