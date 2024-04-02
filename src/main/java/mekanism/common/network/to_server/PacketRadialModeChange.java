package mekanism.common.network.to_server;

import java.util.List;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.INestedRadialMode;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.common.Mekanism;
import mekanism.common.lib.radial.IGenericRadialModeItem;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRadialModeChange(EquipmentSlot slot, List<ResourceLocation> path, int networkRepresentation) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("radial_mode");

    public PacketRadialModeChange(FriendlyByteBuf buffer) {
        this(buffer.readEnum(EquipmentSlot.class), buffer.readList(FriendlyByteBuf::readResourceLocation), buffer.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    @SuppressWarnings("ConstantConditions")//not null, validated by hasNestedData
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
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
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(slot);
        buffer.writeCollection(this.path, FriendlyByteBuf::writeResourceLocation);
        buffer.writeVarInt(networkRepresentation);
    }
}
