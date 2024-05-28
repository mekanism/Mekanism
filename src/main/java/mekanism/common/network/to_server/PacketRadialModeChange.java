package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.List;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRadialModeChange(EquipmentSlot slot, List<ResourceLocation> path, int networkRepresentation) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketRadialModeChange> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("radial_mode"));
    public static final StreamCodec<ByteBuf, PacketRadialModeChange> STREAM_CODEC = StreamCodec.composite(
          PacketUtils.EQUIPMENT_SLOT_STREAM_CODEC, PacketRadialModeChange::slot,
          ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketRadialModeChange::path,
          ByteBufCodecs.VAR_INT, PacketRadialModeChange::networkRepresentation,
          PacketRadialModeChange::new
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketRadialModeChange> type() {
        return TYPE;
    }

    @Override
    @SuppressWarnings("ConstantConditions")//not null, validated by hasNestedData
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemBySlot(slot);
        if (!stack.isEmpty() && stack.getItem() instanceof IGenericRadialModeItem radialModeItem) {
            RadialData<?> radialData = radialModeItem.getRadialData(stack);
            if (radialData != null) {
                for (ResourceLocation path : path) {
                    INestedRadialMode nestedData = radialData.fromIdentifier(path);
                    if (nestedData == null || !nestedData.hasNestedData()) {
                        Mekanism.logger.warn("Could not find path ({}) in current radial data.", path);
                        return;
                    }
                    radialData = nestedData.nestedData();
                }
                setMode(player, stack, radialModeItem, radialData);
            }
        }
    }

    private <MODE extends IRadialMode> void setMode(Player player, ItemStack stack, IGenericRadialModeItem item, RadialData<MODE> radialData) {
        MODE newMode = radialData.fromNetworkRepresentation(networkRepresentation);
        if (newMode != null) {
            item.setMode(stack, player, radialData, newMode);
        }
    }
}
