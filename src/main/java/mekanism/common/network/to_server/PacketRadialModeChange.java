package mekanism.common.network.to_server;

import java.util.List;
import mekanism.common.Mekanism;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.api.radial.RadialData;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketRadialModeChange implements IMekanismPacket {

    private final List<ResourceLocation> path;
    private final EquipmentSlot slot;
    private final int networkRepresentation;

    public PacketRadialModeChange(EquipmentSlot slot, List<ResourceLocation> path, int networkRepresentation) {
        this.slot = slot;
        this.path = path;
        this.networkRepresentation = networkRepresentation;
    }

    @Override
    @SuppressWarnings("ConstantConditions")//not null, validated by hasNestedData
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
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
    }

    private <MODE extends IRadialMode> void setMode(Player player, ItemStack stack, IGenericRadialModeItem item, RadialData<MODE> radialData) {
        MODE newMode = radialData.fromNetworkRepresentation(networkRepresentation);
        if (newMode != null) {
            item.setMode(stack, player, radialData, newMode);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(slot);
        buffer.writeCollection(this.path, FriendlyByteBuf::writeResourceLocation);
        buffer.writeVarInt(networkRepresentation);
    }

    public static PacketRadialModeChange decode(FriendlyByteBuf buffer) {
        EquipmentSlot slot = buffer.readEnum(EquipmentSlot.class);
        List<ResourceLocation> path = buffer.readList(FriendlyByteBuf::readResourceLocation);
        int networkRepresentation = buffer.readVarInt();
        return new PacketRadialModeChange(slot, path, networkRepresentation);
    }
}
