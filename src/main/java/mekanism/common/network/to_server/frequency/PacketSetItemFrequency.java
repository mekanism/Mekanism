package mekanism.common.network.to_server.frequency;

import io.netty.buffer.ByteBuf;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketSetItemFrequency(boolean set, TypedIdentity data, InteractionHand currentHand) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketSetItemFrequency> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("set_item_frequency"));
    public static final StreamCodec<ByteBuf, PacketSetItemFrequency> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.BOOL, PacketSetItemFrequency::set,
          TypedIdentity.STREAM_CODEC, PacketSetItemFrequency::data,
          PacketUtils.INTERACTION_HAND_STREAM_CODEC, PacketSetItemFrequency::currentHand,
          PacketSetItemFrequency::new
    );

    public PacketSetItemFrequency(boolean set, FrequencyType<?> frequencyType, FrequencyIdentity data, InteractionHand currentHand) {
        this(set, new TypedIdentity(frequencyType, data), currentHand);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketSetItemFrequency> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(currentHand);
        if (stack.getItem() instanceof IFrequencyItem frequencyItem && IItemSecurityUtils.INSTANCE.canAccess(player, stack)) {
            updateFrequency(player, stack, frequencyItem.getFrequencyType());
        }
    }

    private <FREQ extends Frequency> void updateFrequency(Player player, ItemStack stack, FrequencyType<FREQ> frequencyType) {
        DataComponentType<FrequencyAware<FREQ>> frequencyComponent = MekanismDataComponents.getFrequencyComponent(frequencyType);
        if (frequencyComponent != null) {
            if (set) {
                stack.set(frequencyComponent, FrequencyAware.create(frequencyType, data.data(), player.getUUID()));
            } else {
                FrequencyAware<FREQ> frequencyAware = stack.get(frequencyComponent);
                FrequencyManager<?> manager = frequencyType.getManager(data.data(), data.data().ownerUUID() == null ? player.getUUID() : data.data().ownerUUID());
                if (manager.remove(data.data().key(), player.getUUID()) && frequencyAware != null && frequencyAware.identity().filter(data.data()::equals).isPresent()) {
                    //If the frequency we are removing matches the stored frequency, remove it
                    stack.remove(frequencyComponent);
                }
            }
        }
    }
}