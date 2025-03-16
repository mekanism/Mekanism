package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.item.interfaces.IModeItem.DisplayChange;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketModeChangeCurios(String slotType, int slot, int shift, boolean displayChangeMessage) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketModeChangeCurios> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("mode_curios"));
    public static final StreamCodec<FriendlyByteBuf, PacketModeChangeCurios> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.STRING_UTF8, PacketModeChangeCurios::slotType,
          ByteBufCodecs.VAR_INT, PacketModeChangeCurios::slot,
          ByteBufCodecs.VAR_INT, PacketModeChangeCurios::shift,
          ByteBufCodecs.BOOL, PacketModeChangeCurios::displayChangeMessage,
          PacketModeChangeCurios::new
    );

    public PacketModeChangeCurios(String slotType, int slot, boolean holdingShift) {
        this(slotType, slot, holdingShift ? -1 : 1, true);
    }

    public PacketModeChangeCurios(String slotType, int slot, int shift) {
        this(slotType, slot, shift, false);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketModeChangeCurios> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (Mekanism.hooks.curios.isLoaded()) {
            Player player = context.player();
            ItemStack stack = CuriosIntegration.getCurioStack(player, slotType, slot);
            if (!stack.isEmpty() && stack.getItem() instanceof IModeItem modeItem) {
                modeItem.changeMode(player, stack, shift, displayChangeMessage ? DisplayChange.OTHER : DisplayChange.NONE);
            }
        }
    }
}