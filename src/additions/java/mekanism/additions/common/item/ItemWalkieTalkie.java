package mekanism.additions.common.item;

import java.util.List;
import java.util.Objects;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.additions.common.registries.AdditionsAttachmentTypes;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemWalkieTalkie extends Item implements IModeItem {

    public ItemWalkieTalkie(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        WalkieData data = stack.getData(AdditionsAttachmentTypes.WALKIE_DATA);
        tooltip.add(OnOff.of(data.isRunning(), true).getTextComponent());
        tooltip.add(AdditionsLang.CHANNEL.translateColored(EnumColor.DARK_AQUA, EnumColor.GRAY, data.getChannel()));
        if (!MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            tooltip.add(AdditionsLang.WALKIE_DISABLED.translateColored(EnumColor.DARK_RED));
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            WalkieData data = stack.getData(AdditionsAttachmentTypes.WALKIE_DATA);
            data.running = !data.isRunning();
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, DisplayChange displayChange) {
        WalkieData data = stack.getData(AdditionsAttachmentTypes.WALKIE_DATA);
        if (data.isRunning()) {
            int newChannel = Math.floorMod(data.getChannel() + shift - 1, 8) + 1;
            if (data.getChannel() != newChannel) {
                data.channel = newChannel;
                displayChange.sendMessage(player, newChannel, AdditionsLang.CHANNEL_CHANGE::translate);
            }
        }
    }

    @NotNull
    @Override
    public Component getScrollTextComponent(@NotNull ItemStack stack) {
        WalkieData data = stack.getData(AdditionsAttachmentTypes.WALKIE_DATA);
        return AdditionsLang.CHANNEL.translateColored(EnumColor.GRAY, EnumColor.WHITE, data.getChannel());
    }

    public static class WalkieData implements INBTSerializable<CompoundTag> {

        @Nullable
        public static WalkieData get(ItemStack stack) {
            if (stack.getItem() instanceof ItemWalkieTalkie) {
                return stack.getData(AdditionsAttachmentTypes.WALKIE_DATA);
            }
            return null;
        }

        private int channel;
        private boolean running;

        public WalkieData() {
            this(1, false);
        }

        private WalkieData(int channel, boolean running) {
            this.channel = channel;
            this.running = running;
        }

        public boolean isRunning() {
            return running;
        }

        public int getChannel() {
            return channel;
        }

        @Nullable
        public WalkieData copy(IAttachmentHolder holder) {
            if (channel == 1 && !running) {
                return null;
            }
            return new WalkieData(channel, running);
        }

        @Nullable
        @Override
        public CompoundTag serializeNBT() {
            if (channel == 1 && !running) {
                return null;
            }
            CompoundTag nbt = new CompoundTag();
            nbt.putInt(NBTConstants.CHANNEL, channel);
            nbt.putBoolean(NBTConstants.RUNNING, running);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            channel = Math.max(1, nbt.getInt(NBTConstants.CHANNEL));
            running = nbt.getBoolean(NBTConstants.RUNNING);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            WalkieData other = (WalkieData) o;
            return channel == other.channel && running == other.running;
        }

        @Override
        public int hashCode() {
            return Objects.hash(channel, running);
        }
    }
}