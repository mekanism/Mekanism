package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/// Used for informing the server that an action including an item happened in a tile GUI
public record PacketTileGuiInteractItem(GuiInteractionItem itemInteraction, BlockPos tilePosition, ItemStack extraItem) implements IMekanismPacket {

    public static final Type<PacketTileGuiInteractItem> TYPE = new Type<>(Mekanism.rl("tile_gui_interact_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketTileGuiInteractItem> STREAM_CODEC = StreamCodec.composite(
          GuiInteractionItem.STREAM_CODEC, packet -> packet.itemInteraction,
          BlockPos.STREAM_CODEC, packet -> packet.tilePosition,
          ItemStack.OPTIONAL_STREAM_CODEC, packet -> packet.extraItem,
          PacketTileGuiInteractItem::new
    );

    public PacketTileGuiInteractItem(GuiInteractionItem interaction, BlockEntity tile, ItemStack stack) {
        this(interaction, tile.getBlockPos(), stack);
    }

    @Override
    public Type<PacketTileGuiInteractItem> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level(), tilePosition);
        if (tile != null) {
            itemInteraction.consume(tile, player, extraItem);
        }
    }

    public enum GuiInteractionItem {
        DIGITAL_MINER_INVERSE_REPLACE_ITEM((tile, _, stack) -> {
            if (tile instanceof TileEntityDigitalMiner miner) {
                miner.setInverseReplaceTarget(stack.getItem());
            }
        }),
        QIO_REDSTONE_ADAPTER_STACK((tile, _, stack) -> {
            if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
                redstoneAdapter.handleStackChange(stack);
            }
        });

        public static final IntFunction<GuiInteractionItem> BY_ID = ByIdMap.continuous(GuiInteractionItem::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiInteractionItem> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiInteractionItem::ordinal);

        private final TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile;

        GuiInteractionItem(TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, ItemStack stack) {
            consumerForTile.accept(tile, player, stack);
        }
    }
}