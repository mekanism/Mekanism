package mekanism.common.network.to_server.button;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketItemButtonPress(ClickedItemButton buttonClicked, InteractionHand hand) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketItemButtonPress> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("item_button"));
    public static final StreamCodec<ByteBuf, PacketItemButtonPress> STREAM_CODEC = StreamCodec.composite(
          ClickedItemButton.STREAM_CODEC, PacketItemButtonPress::buttonClicked,
          InteractionHand.STREAM_CODEC, PacketItemButtonPress::hand,
          PacketItemButtonPress::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketItemButtonPress> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemAccess itemAccess = ItemAccessUtils.playerHandAccess(player, hand);
        ItemResource itemType = itemAccess.getResource();
        if (itemType.getItem() instanceof IGuiItem) {
            MenuProvider provider = buttonClicked.getProvider(itemType, hand);
            if (provider != null) {
                player.openMenu(provider, buf -> {
                    buf.writeEnum(hand);
                    buttonClicked.encodeExtraData(buf, itemType);
                });
            }
        }
    }

    public enum ClickedItemButton {
        BACK_BUTTON((itemType, hand) -> {
            //Note: This should always be true, as otherwise we wouldn't have a provider at the various call sites
            if (itemType.getItem() instanceof IGuiItem guiItem) {
                return guiItem.getContainerType().getProvider(itemType.getHoverName(), hand, itemType);
            }
            return null;
        }, (buffer, itemType) -> {
            //Note: This should always be true, as otherwise we wouldn't have a provider at the various call sites
            if (itemType.getItem() instanceof IGuiItem guiItem) {
                //Mirror the logic from ContainerRegistryObject#tryOpenGui so that we properly reinitialize the initial GUI
                guiItem.encodeContainerData(buffer, itemType);
            }
        }),
        QIO_FREQUENCY_SELECT((itemType, hand) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, hand, itemType));

        public static final IntFunction<ClickedItemButton> BY_ID = ByIdMap.continuous(ClickedItemButton::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClickedItemButton> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClickedItemButton::ordinal);

        private final BiFunction<ItemResource, InteractionHand, @Nullable MenuProvider> providerFromItem;
        @Nullable
        private final BiConsumer<RegistryFriendlyByteBuf, ItemResource> extraEncodingData;

        ClickedItemButton(BiFunction<ItemResource, InteractionHand, @Nullable MenuProvider> providerFromItem) {
            this(providerFromItem, null);
        }

        ClickedItemButton(BiFunction<ItemResource, InteractionHand, @Nullable MenuProvider> providerFromItem,
              @Nullable BiConsumer<RegistryFriendlyByteBuf, ItemResource> extraEncodingData) {
            this.providerFromItem = providerFromItem;
            this.extraEncodingData = extraEncodingData;
        }

        @Nullable
        public MenuProvider getProvider(ItemResource itemType, InteractionHand hand) {
            return providerFromItem.apply(itemType, hand);
        }

        private void encodeExtraData(RegistryFriendlyByteBuf buffer, ItemResource itemType) {
            if (extraEncodingData != null) {
                extraEncodingData.accept(buffer, itemType);
            }
        }
    }
}