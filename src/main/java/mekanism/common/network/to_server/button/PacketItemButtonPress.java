package mekanism.common.network.to_server.button;

import java.util.function.BiFunction;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketItemButtonPress(ClickedItemButton buttonClicked, InteractionHand hand) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("item_button");

    public PacketItemButtonPress(FriendlyByteBuf buffer) {
        this(buffer.readEnum(ClickedItemButton.class), buffer.readEnum(InteractionHand.class));
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
            if (stack.getItem() instanceof IGuiItem) {
                player.openMenu(buttonClicked.getProvider(stack, hand), buf -> {
                    buf.writeEnum(hand);
                    buf.writeItem(stack);
                });
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(buttonClicked);
        buffer.writeEnum(hand);
    }

    public enum ClickedItemButton {
        BACK_BUTTON((stack, hand) -> {
            if (stack.getItem() instanceof IGuiItem guiItem) {
                return guiItem.getContainerType().getProvider(stack.getHoverName(), hand, stack);
            }
            return null;
        }),
        QIO_FREQUENCY_SELECT((stack, hand) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, hand, stack));

        private final BiFunction<ItemStack, InteractionHand, @Nullable MenuProvider> providerFromItem;

        ClickedItemButton(BiFunction<ItemStack, InteractionHand, @Nullable MenuProvider> providerFromItem) {
            this.providerFromItem = providerFromItem;
        }

        @Nullable
        public MenuProvider getProvider(ItemStack stack, InteractionHand hand) {
            return providerFromItem.apply(stack, hand);
        }
    }
}