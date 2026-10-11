package mekanism.common.inventory.container;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jspecify.annotations.Nullable;

public class ItemContainerProvider extends ContainerProvider {

    public static final StreamCodec<ByteBuf, InteractionHand> ITEM_DATA = InteractionHand.STREAM_CODEC;

    private final InteractionHand hand;

    public ItemContainerProvider(Component displayName, MenuConstructor provider, boolean resetMousePosition, @Nullable ClientSideData clientSideData,
          InteractionHand hand) {
        super(displayName, provider, resetMousePosition, clientSideData);
        this.hand = hand;
    }

    @Override
    public void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        //Ensure the hand is written as the first piece of data
        ITEM_DATA.encode(buffer, hand);
        super.writeClientSideData(player, menu, buffer);
    }
}