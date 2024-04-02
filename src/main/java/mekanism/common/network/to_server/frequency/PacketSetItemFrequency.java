package mekanism.common.network.to_server.frequency;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public class PacketSetItemFrequency<FREQ extends Frequency> extends PacketSetFrequency<FREQ> {

    public static final ResourceLocation ID = Mekanism.rl("set_item_frequency");

    private final InteractionHand currentHand;

    public PacketSetItemFrequency(FriendlyByteBuf buf) {
        super(buf);
        this.currentHand = buf.readEnum(InteractionHand.class);
    }

    public PacketSetItemFrequency(boolean set, FrequencyType<FREQ> type, FrequencyIdentity data, InteractionHand currentHand) {
        super(set, type, data);
        this.currentHand = currentHand;
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
            ItemStack stack = player.getItemInHand(currentHand);
            if (stack.getItem() instanceof IFrequencyItem && IItemSecurityUtils.INSTANCE.canAccess(player, stack)) {
                FrequencyAware<FREQ> frequencyAware = (FrequencyAware<FREQ>) stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE);
                if (set) {
                    frequencyAware.setFrequency(data, player.getUUID());
                } else {
                    frequencyAware.removeFrequency(data, player.getUUID());
                }
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeEnum(currentHand);
    }
}