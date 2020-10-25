package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiSetFrequency<FREQ extends Frequency> {

    private final FrequencyType<FREQ> type;
    private final FrequencyUpdate updateType;
    private final FrequencyIdentity data;
    private final BlockPos tilePosition;
    private final Hand currentHand;

    private PacketGuiSetFrequency(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, BlockPos tilePosition, Hand currentHand) {
        this.updateType = updateType;
        this.type = type;
        this.data = data;
        this.tilePosition = tilePosition;
        this.currentHand = currentHand;
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, BlockPos tilePosition) {
        return new PacketGuiSetFrequency<>(updateType, type, data, tilePosition, null);
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, Hand currentHand) {
        return new PacketGuiSetFrequency<>(updateType, type, data, null, currentHand);
    }

    public static <FREQ extends Frequency> void handle(PacketGuiSetFrequency<FREQ> message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (message.updateType.isTile()) {
                TileEntity tile = WorldUtils.getTileEntity(player.world, message.tilePosition);
                if (tile instanceof IFrequencyHandler) {
                    if (message.updateType == FrequencyUpdate.SET_TILE) {
                        ((IFrequencyHandler) tile).setFrequency(message.type, message.data);
                    } else if (message.updateType == FrequencyUpdate.REMOVE_TILE) {
                        ((IFrequencyHandler) tile).removeFrequency(message.type, message.data);
                    }
                }
            } else {
                FrequencyManager<FREQ> manager = message.type.getManager(message.data.isPublic() ? null : player.getUniqueID());
                ItemStack stack = player.getHeldItem(message.currentHand);
                if (stack.getItem() instanceof IFrequencyItem) {
                    IFrequencyItem item = (IFrequencyItem) stack.getItem();
                    FREQ toUse = null;
                    if (message.updateType == FrequencyUpdate.SET_ITEM) {
                        toUse = manager.getOrCreateFrequency(message.data, player.getUniqueID());
                        item.setFrequency(stack, toUse);
                    } else if (message.updateType == FrequencyUpdate.REMOVE_ITEM) {
                        manager.remove(message.data.getKey(), player.getUniqueID());
                        FrequencyIdentity current = item.getFrequency(stack);
                        if (current != null) {
                            if (current.equals(message.data)) {
                                //If the frequency we are removing matches the stored frequency set it to nothing
                                item.setFrequency(stack, null);
                            } else {
                                //Otherwise just delete the frequency and keep what the item is set to
                                FrequencyManager<FREQ> currentManager = manager;
                                if (message.data.isPublic() != current.isPublic()) {
                                    //Update the manager if it is the wrong one for getting our actual current frequency
                                    currentManager = message.type.getManager(current.isPublic() ? null : player.getUniqueID());
                                }
                                toUse = currentManager.getFrequency(current.getKey());
                            }
                        }
                    }
                    Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(message.currentHand, message.type, player.getUniqueID(), toUse), player);
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static <FREQ extends Frequency> void encode(PacketGuiSetFrequency<FREQ> pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.updateType);
        pkt.type.write(buf);
        pkt.type.getIdentitySerializer().write(buf, pkt.data);
        if (pkt.updateType.isTile()) {
            buf.writeBlockPos(pkt.tilePosition);
        } else {
            buf.writeEnumValue(pkt.currentHand);
        }
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> decode(PacketBuffer buf) {
        FrequencyUpdate updateType = buf.readEnumValue(FrequencyUpdate.class);
        FrequencyType<FREQ> type = FrequencyType.load(buf);
        FrequencyIdentity data = type.getIdentitySerializer().read(buf);
        BlockPos pos = updateType.isTile() ? buf.readBlockPos() : null;
        Hand hand = !updateType.isTile() ? buf.readEnumValue(Hand.class) : null;
        return new PacketGuiSetFrequency<>(updateType, type, data, pos, hand);
    }

    public enum FrequencyUpdate {
        SET_TILE,
        SET_ITEM,
        REMOVE_TILE,
        REMOVE_ITEM;

        boolean isTile() {
            return this == SET_TILE || this == REMOVE_TILE;
        }
    }
}