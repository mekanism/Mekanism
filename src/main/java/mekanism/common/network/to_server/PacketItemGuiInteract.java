package mekanism.common.network.to_server;

import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand, int extra) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("item_gui_interact");

    public PacketItemGuiInteract(FriendlyByteBuf buffer) {
        this(buffer.readEnum(ItemGuiInteraction.class), buffer.readEnum(InteractionHand.class), buffer.readVarInt());
    }

    public PacketItemGuiInteract(ItemGuiInteraction interaction, InteractionHand hand) {
        this(interaction, hand, 0);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                interaction.consume(stack, player, extra);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(interaction);
        buffer.writeEnum(hand);
        buffer.writeVarInt(extra);
    }

    public enum ItemGuiInteraction {
        TARGET_DIRECTION_BUTTON((stack, player, extra) -> stack.setData(MekanismAttachmentTypes.INSERT_INTO_FREQUENCY, !stack.getData(MekanismAttachmentTypes.INSERT_INTO_FREQUENCY))),

        NEXT_SECURITY_MODE((stack, player, extra) -> SecurityUtils.get().incrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack))),
        PREVIOUS_SECURITY_MODE((stack, player, extra) -> SecurityUtils.get().decrementSecurityMode(player, IItemSecurityUtils.INSTANCE.securityCapability(stack)))
        ;


        private final TriConsumer<ItemStack, Player, Integer> consumerForTile;

        ItemGuiInteraction(TriConsumer<ItemStack, Player, Integer> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(ItemStack stack, Player player, int extra) {
            consumerForTile.accept(stack, player, extra);
        }
    }
}