package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.util.text.InputValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketAddTrusted(BlockPos pos, String name) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("add_trusted");

    public PacketAddTrusted(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readUtf(Player.MAX_NAME_LENGTH));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        if (!name.isEmpty() && InputValidator.test(name, InputValidator.USERNAME)) {
            TileEntitySecurityDesk tile = PacketUtils.blockEntity(context, pos, TileEntitySecurityDesk.class);
            if (tile != null) {
                tile.addTrusted(name);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(name, Player.MAX_NAME_LENGTH);
    }
}