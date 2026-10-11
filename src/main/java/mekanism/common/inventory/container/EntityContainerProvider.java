package mekanism.common.inventory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jspecify.annotations.Nullable;

public class EntityContainerProvider extends ContainerProvider {

    private final int entityId;

    public EntityContainerProvider(Component displayName, MenuConstructor provider, boolean resetMousePosition, @Nullable ClientSideData clientSideData, Entity entity) {
        super(displayName, provider, resetMousePosition, clientSideData);
        this.entityId = entity.getId();
    }

    @Override
    public void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        //Ensure the entity id is written as the first piece of data
        buffer.writeVarInt(entityId);
        super.writeClientSideData(player, menu, buffer);
    }
}