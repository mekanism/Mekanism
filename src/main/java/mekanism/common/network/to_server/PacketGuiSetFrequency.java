package mekanism.common.network.to_server;

import mekanism.common.Mekanism;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.to_client.PacketFrequencyItemGuiUpdate;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketGuiSetFrequency<FREQ extends Frequency> implements IMekanismPacket {

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

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player == null) {
            return;
        }
        if (updateType.isTile()) {
            TileEntity tile = WorldUtils.getTileEntity(player.level, tilePosition);
            if (tile instanceof IFrequencyHandler) {
                if (updateType == FrequencyUpdate.SET_TILE) {
                    ((IFrequencyHandler) tile).setFrequency(type, data);
                } else if (updateType == FrequencyUpdate.REMOVE_TILE) {
                    ((IFrequencyHandler) tile).removeFrequency(type, data);
                }
            }
        } else {
            FrequencyManager<FREQ> manager = type.getManager(data.isPublic() ? null : player.getUUID());
            ItemStack stack = player.getItemInHand(currentHand);
            if (stack.getItem() instanceof IFrequencyItem) {
                IFrequencyItem item = (IFrequencyItem) stack.getItem();
                FREQ toUse = null;
                if (updateType == FrequencyUpdate.SET_ITEM) {
                    toUse = manager.getOrCreateFrequency(data, player.getUUID());
                    item.setFrequency(stack, toUse);
                } else if (updateType == FrequencyUpdate.REMOVE_ITEM) {
                    manager.remove(data.getKey(), player.getUUID());
                    FrequencyIdentity current = item.getFrequency(stack);
                    if (current != null) {
                        if (current.equals(data)) {
                            //If the frequency we are removing matches the stored frequency set it to nothing
                            item.setFrequency(stack, null);
                        } else {
                            //Otherwise just delete the frequency and keep what the item is set to
                            FrequencyManager<FREQ> currentManager = manager;
                            if (data.isPublic() != current.isPublic()) {
                                //Update the manager if it is the wrong one for getting our actual current frequency
                                currentManager = type.getManager(current.isPublic() ? null : player.getUUID());
                            }
                            toUse = currentManager.getFrequency(current.getKey());
                        }
                    }
                }
                Mekanism.packetHandler.sendTo(PacketFrequencyItemGuiUpdate.update(currentHand, type, player.getUUID(), toUse), player);
            }
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(updateType);
        type.write(buffer);
        type.getIdentitySerializer().write(buffer, data);
        if (updateType.isTile()) {
            buffer.writeBlockPos(tilePosition);
        } else {
            buffer.writeEnum(currentHand);
        }
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> decode(PacketBuffer buffer) {
        FrequencyUpdate updateType = buffer.readEnum(FrequencyUpdate.class);
        FrequencyType<FREQ> type = FrequencyType.load(buffer);
        FrequencyIdentity data = type.getIdentitySerializer().read(buffer);
        BlockPos pos = updateType.isTile() ? buffer.readBlockPos() : null;
        Hand hand = !updateType.isTile() ? buffer.readEnum(Hand.class) : null;
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