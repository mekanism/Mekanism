package mekanism.common.network.to_server.frequency;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
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
        context.player().ifPresent(player -> {
            ItemStack stack = player.getItemInHand(currentHand);
            if (stack.getItem() instanceof IFrequencyItem && IItemSecurityUtils.INSTANCE.canAccess(player, stack)) {
                FrequencyManager<FREQ> manager = type.getManager(data, player.getUUID());
                FrequencyAware<FREQ> frequencyAware = (FrequencyAware<FREQ>) stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE);
                if (set) {
                    //Note: We don't bother validating if the frequency is public or not here, as if it isn't then
                    // a new private frequency will just be created for the player who sent a packet they shouldn't
                    // have been able to send due to not knowing what private frequencies exist for other players
                    frequencyAware.setFrequency(manager.getOrCreateFrequency(data, player.getUUID()));
                } else if (manager.remove(data.key(), player.getUUID())) {
                    FrequencyIdentity current = frequencyAware.getIdentity();
                    if (current != null && current.equals(data)) {
                        //If the frequency we are removing matches the stored frequency set it to nothing
                        frequencyAware.setFrequency(null);
                    }
                }
            }
        });
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeEnum(currentHand);
    }
}