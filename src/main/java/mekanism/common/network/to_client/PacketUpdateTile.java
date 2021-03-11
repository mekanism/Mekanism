package mekanism.common.network.to_client;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketUpdateTile implements IMekanismPacket {

    private final CompoundNBT updateTag;
    private final BlockPos pos;

    public PacketUpdateTile(TileEntityUpdateable tile) {
        this(tile.getBlockPos(), tile.getReducedUpdateTag());
    }

    private PacketUpdateTile(BlockPos pos, CompoundNBT updateTag) {
        this.pos = pos;
        this.updateTag = updateTag;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ClientWorld world = Minecraft.getInstance().level;
        if (world != null) {
            TileEntityUpdateable tile = WorldUtils.getTileEntity(TileEntityUpdateable.class, world, pos, true);
            if (tile == null) {
                Mekanism.logger.info("Update tile packet received for position: {} in world: {}, but no valid tile was found.", pos,
                      world.dimension().location());
            } else {
                tile.handleUpdatePacket(updateTag);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(updateTag);
    }

    public static PacketUpdateTile decode(PacketBuffer buffer) {
        return new PacketUpdateTile(buffer.readBlockPos(), buffer.readNbt());
    }
}