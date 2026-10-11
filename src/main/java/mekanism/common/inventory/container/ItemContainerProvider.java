package mekanism.common.inventory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jspecify.annotations.Nullable;

public class ItemContainerProvider extends ContainerProvider {

    @Nullable
    private final InteractionHand hand;

    public ItemContainerProvider(Component displayName, MenuConstructor provider, boolean resetMousePosition, @Nullable ClientSideData clientSideData,
          @Nullable InteractionHand hand) {
        super(displayName, provider, resetMousePosition, clientSideData);
        this.hand = hand;
    }

    @Override
    public void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        //Ensure the hand is written as the first piece of data
        if (hand != null) {
            InteractionHand.STREAM_CODEC.encode(buffer, hand);
        }
        super.writeClientSideData(player, menu, buffer);
    }
}