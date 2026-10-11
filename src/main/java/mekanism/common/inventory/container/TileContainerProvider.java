package mekanism.common.inventory.container;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public class TileContainerProvider extends ContainerProvider {

    public static final StreamCodec<ByteBuf, BlockPos> TILE_DATA = BlockPos.STREAM_CODEC;

    private final BlockPos pos;

    public TileContainerProvider(Component displayName, MenuConstructor provider, boolean resetMousePosition, @Nullable ClientSideData clientSideData, BlockEntity tile) {
        super(displayName, provider, resetMousePosition, clientSideData);
        this.pos = tile.getBlockPos();
    }

    @Override
    public void writeClientSideData(ServerPlayer player, AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        //Ensure the position is written as the first piece of data
        TILE_DATA.encode(buffer, this.pos);
        super.writeClientSideData(player, menu, buffer);
    }
}