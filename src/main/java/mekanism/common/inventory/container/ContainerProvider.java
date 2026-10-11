package mekanism.common.inventory.container;

import mekanism.api.text.ILangEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jspecify.annotations.Nullable;

public class ContainerProvider implements MenuProvider {

    @Nullable
    private final ClientSideData clientSideData;
    private final boolean resetMousePosition;
    private final MenuConstructor provider;
    private final Component displayName;

    public ContainerProvider(ILangEntry translationHelper, MenuConstructor provider) {
        this(translationHelper.translate(), provider, true, null);
    }

    public ContainerProvider(Component displayName, MenuConstructor provider, boolean resetMousePosition, @Nullable ClientSideData clientSideData) {
        this.displayName = displayName;
        this.provider = provider;
        this.resetMousePosition = resetMousePosition;
        this.clientSideData = clientSideData;
    }

    @Override
    public boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return resetMousePosition;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player player) {
        return provider.createMenu(containerId, inv, player);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        if (clientSideData != null) {
            clientSideData.writeData(player, menu, buffer);
        }
    }

    @FunctionalInterface
    public interface ClientSideData {

        void writeData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer);
    }
}