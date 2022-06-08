package mekanism.common.network.to_server;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketRemoveModule implements IMekanismPacket {

    private final BlockPos pos;
    private final ModuleData<?> moduleType;

    public PacketRemoveModule(BlockPos pos, ModuleData<?> type) {
        this.pos = pos;
        moduleType = type;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            TileEntityModificationStation tile = WorldUtils.getTileEntity(TileEntityModificationStation.class, player.level, pos);
            if (tile != null) {
                tile.removeModule(player, moduleType);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeRegistryId(MekanismAPI.moduleRegistry(), moduleType);
    }

    public static PacketRemoveModule decode(FriendlyByteBuf buffer) {
        return new PacketRemoveModule(buffer.readBlockPos(), buffer.readRegistryIdSafe(ModuleData.class));
    }
}
