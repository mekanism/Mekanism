package mekanism.common.network.to_server.configuration_update;

import mekanism.api.RelativeSide;
import mekanism.common.Mekanism;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.MekClickType;
import mekanism.common.network.PacketUtils;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketInputColor(BlockPos pos, MekClickType clickType, RelativeSide inputSide) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("input_color");

    public PacketInputColor(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readEnum(MekClickType.class), buffer.readEnum(RelativeSide.class));
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        TileComponentEjector ejector = PacketUtils.ejector(context, pos);
        if (ejector != null) {
            ejector.setInputColor(inputSide, switch (clickType) {
                case LEFT -> TransporterUtils.increment(ejector.getInputColor(inputSide));
                case RIGHT -> TransporterUtils.decrement(ejector.getInputColor(inputSide));
                case SHIFT_LEFT -> null;
            });
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(clickType);
        buffer.writeEnum(inputSide);
    }
}