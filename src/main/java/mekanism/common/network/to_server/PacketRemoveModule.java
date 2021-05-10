package mekanism.common.network.to_server;

import mekanism.api.gear.ModuleData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketRemoveModule implements IMekanismPacket {

    private final BlockPos pos;
    private final ModuleData<?> moduleType;

    public PacketRemoveModule(BlockPos pos, ModuleData<?> type) {
        this.pos = pos;
        moduleType = type;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        PlayerEntity player = context.getSender();
        if (player != null) {
            TileEntityModificationStation tile = WorldUtils.getTileEntity(TileEntityModificationStation.class, player.level, pos);
            if (tile != null) {
                tile.removeModule(player, moduleType);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeRegistryId(moduleType);
    }

    public static PacketRemoveModule decode(PacketBuffer buffer) {
        return new PacketRemoveModule(buffer.readBlockPos(), buffer.readRegistryId());
    }
}
