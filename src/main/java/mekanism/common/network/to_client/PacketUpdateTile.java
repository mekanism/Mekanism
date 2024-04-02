package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketUpdateTile(BlockPos pos, CompoundTag updateTag) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("update_tile");

    public PacketUpdateTile(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readNbt());
    }

    public PacketUpdateTile(TileEntityUpdateable tile) {
        this(tile.getBlockPos(), tile.getReducedUpdateTag());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Level world = context.level().orElse(null);
        //Only handle the update packet if the block is currently loaded (otherwise we would have the warning get logged in cases we don't want it to)
        if (WorldUtils.isBlockLoaded(world, pos)) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos, true);
            if (tile == null) {
                Mekanism.logger.warn("Update tile packet received for position: {} in world: {}, but no valid tile was found.", pos,
                      world.dimension().location());
            } else {
                tile.handleUpdatePacket(updateTag);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(updateTag);
    }
}