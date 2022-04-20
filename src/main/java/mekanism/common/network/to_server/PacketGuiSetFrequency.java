package mekanism.common.network.to_server;

import mekanism.api.MekanismAPI;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PacketGuiSetFrequency<FREQ extends Frequency> implements IMekanismPacket {

    private final FrequencyType<FREQ> type;
    private final FrequencyUpdate updateType;
    private final FrequencyIdentity data;
    private final BlockPos tilePosition;
    private final InteractionHand currentHand;

    private PacketGuiSetFrequency(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, BlockPos tilePosition, InteractionHand currentHand) {
        this.updateType = updateType;
        this.type = type;
        this.data = data;
        this.tilePosition = tilePosition;
        this.currentHand = currentHand;
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, BlockPos tilePosition) {
        return new PacketGuiSetFrequency<>(updateType, type, data, tilePosition, null);
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> create(FrequencyUpdate updateType, FrequencyType<FREQ> type, FrequencyIdentity data, InteractionHand currentHand) {
        return new PacketGuiSetFrequency<>(updateType, type, data, null, currentHand);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }
        if (updateType.isTile()) {
            BlockEntity tile = WorldUtils.getTileEntity(player.level, tilePosition);
            if (tile instanceof IFrequencyHandler frequencyHandler && MekanismAPI.getSecurityUtils().canAccess(player, tile)) {
                if (updateType == FrequencyUpdate.SET_TILE) {
                    frequencyHandler.setFrequency(type, data, player.getUUID());
                } else if (updateType == FrequencyUpdate.REMOVE_TILE) {
                    frequencyHandler.removeFrequency(type, data, player.getUUID());
                }
            }
        } else {
            ItemStack stack = player.getItemInHand(currentHand);
            if (MekanismAPI.getSecurityUtils().canAccess(player, stack) && stack.getItem() instanceof IFrequencyItem item) {
                FrequencyManager<FREQ> manager = type.getManager(data, player.getUUID());
                if (updateType == FrequencyUpdate.SET_ITEM) {
                    //Note: We don't bother validating if the frequency is public or not here, as if it isn't then
                    // a new private frequency will just be created for the player who sent a packet they shouldn't
                    // have been able to send due to not knowing what private frequencies exist for other players
                    item.setFrequency(stack, manager.getOrCreateFrequency(data, player.getUUID()));
                } else if (updateType == FrequencyUpdate.REMOVE_ITEM) {
                    if (manager.remove(data.key(), player.getUUID())) {
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
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(updateType);
        type.write(buffer);
        type.getIdentitySerializer().write(buffer, data);
        if (updateType.isTile()) {
            buffer.writeBlockPos(tilePosition);
        } else {
            buffer.writeEnum(currentHand);
        }
    }

    public static <FREQ extends Frequency> PacketGuiSetFrequency<FREQ> decode(FriendlyByteBuf buffer) {
        FrequencyUpdate updateType = buffer.readEnum(FrequencyUpdate.class);
        FrequencyType<FREQ> type = FrequencyType.load(buffer);
        FrequencyIdentity data = type.getIdentitySerializer().read(buffer);
        BlockPos pos = updateType.isTile() ? buffer.readBlockPos() : null;
        InteractionHand hand = updateType.isTile() ? null : buffer.readEnum(InteractionHand.class);
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