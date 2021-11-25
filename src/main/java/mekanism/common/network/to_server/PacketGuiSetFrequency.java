package mekanism.common.network.to_server;

import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.SecurityUtils;
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
            if (SecurityUtils.canAccess(player, tile) && tile instanceof IFrequencyHandler) {
                if (updateType == FrequencyUpdate.SET_TILE) {
                    ((IFrequencyHandler) tile).setFrequency(type, data, player.getUUID());
                } else if (updateType == FrequencyUpdate.REMOVE_TILE) {
                    ((IFrequencyHandler) tile).removeFrequency(type, data, player.getUUID());
                }
            }
        } else {
            ItemStack stack = player.getItemInHand(currentHand);
            if (SecurityUtils.canAccess(player, stack) && stack.getItem() instanceof IFrequencyItem) {
                IFrequencyItem item = (IFrequencyItem) stack.getItem();
                FrequencyManager<FREQ> manager = type.getManager(data, player.getUUID());
                if (updateType == FrequencyUpdate.SET_ITEM) {
                    //Note: We don't bother validating if the frequency is public or not here, as if it isn't then
                    // a new private frequency will just be created for the player who sent a packet they shouldn't
                    // have been able to send due to not knowing what private frequencies exist for other players
                    item.setFrequency(stack, manager.getOrCreateFrequency(data, player.getUUID()));
                } else if (updateType == FrequencyUpdate.REMOVE_ITEM) {
                    if (manager.remove(data.getKey(), player.getUUID())) {
                        FrequencyIdentity current = item.getFrequencyIdentity(stack);
                        if (current != null && current.equals(data)) {
                            //If the frequency we are removing matches the stored frequency set it to nothing
                            item.setFrequency(stack, null);
                        }
                    }
                }
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
        Hand hand = updateType.isTile() ? null : buffer.readEnum(Hand.class);
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