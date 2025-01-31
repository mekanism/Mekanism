package mekanism.common.network.to_client;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketUpdateTile(BlockPos pos, CompoundTag updateTag) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketUpdateTile> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("update_tile"));
    public static final StreamCodec<ByteBuf, PacketUpdateTile> STREAM_CODEC = StreamCodec.composite(
          BlockPos.STREAM_CODEC, PacketUpdateTile::pos,
          ByteBufCodecs.TRUSTED_COMPOUND_TAG, PacketUpdateTile::updateTag,
          PacketUpdateTile::new
    );

    public PacketUpdateTile(TileEntityUpdateable tile) {
        this(tile.getBlockPos(), tile.getReducedUpdateTag(tile.getLevel().registryAccess()));
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketUpdateTile> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Level world = context.player().level();
        //Only handle the update packet if the block is currently loaded (otherwise we would have the warning get logged in cases we don't want it to)
        if (WorldUtils.isBlockLoaded(world, pos)) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos, true);
            if (tile == null) {
                Mekanism.logger.warn("Update tile packet received for position: {} in world: {}, but no valid tile was found.", pos,
                      world.dimension().location());
            } else {
                tile.handleUpdateTag(updateTag, world.registryAccess());
            }
        }
    }
}