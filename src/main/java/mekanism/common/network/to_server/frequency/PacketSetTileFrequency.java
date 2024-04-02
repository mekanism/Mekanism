package mekanism.common.network.to_server.frequency;

import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketSetTileFrequency<FREQ extends Frequency> extends PacketSetFrequency<FREQ> {

    public static final ResourceLocation ID = Mekanism.rl("set_tile_frequency");

    private final BlockPos pos;

    public PacketSetTileFrequency(FriendlyByteBuf buf) {
        super(buf);
        this.pos = buf.readBlockPos();
    }

    public PacketSetTileFrequency(boolean set, FrequencyType<FREQ> type, FrequencyIdentity data, BlockPos pos) {
        super(set, type, data);
        this.pos = pos;
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
            BlockEntity tile = WorldUtils.getTileEntity(player.level(), pos);
            if (tile instanceof IFrequencyHandler frequencyHandler && IBlockSecurityUtils.INSTANCE.canAccess(player, player.level(), pos, tile)) {
                if (set) {
                    frequencyHandler.setFrequency(type, data, player.getUUID());
                } else {
                    frequencyHandler.removeFrequency(type, data, player.getUUID());
                }
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeBlockPos(pos);
    }
}