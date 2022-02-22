package mekanism.additions.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemWalkieTalkie extends Item implements IModeItem {

    public ItemWalkieTalkie(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        tooltip.add(OnOff.of(getOn(stack), true).getTextComponent());
        tooltip.add(AdditionsLang.CHANNEL.translateColored(EnumColor.DARK_AQUA, EnumColor.GRAY, getChannel(stack)));
        if (!MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            tooltip.add(AdditionsLang.WALKIE_DISABLED.translateColored(EnumColor.DARK_RED));
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setOn(itemStack, !getOn(itemStack));
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSame(oldStack, newStack);
    }

    public void setOn(ItemStack itemStack, boolean on) {
        ItemDataUtils.setBoolean(itemStack, NBTConstants.RUNNING, on);
    }

    public boolean getOn(ItemStack itemStack) {
        return ItemDataUtils.getBoolean(itemStack, NBTConstants.RUNNING);
    }

    public void setChannel(ItemStack itemStack, int channel) {
        ItemDataUtils.setInt(itemStack, NBTConstants.CHANNEL, channel);
    }

    public int getChannel(ItemStack itemStack) {
        int channel = ItemDataUtils.getInt(itemStack, NBTConstants.CHANNEL);
        if (channel == 0) {
            setChannel(itemStack, 1);
            channel = 1;
        }
        return channel;
    }

    @Override
    public void changeMode(@Nonnull Player player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (getOn(stack)) {
            int channel = getChannel(stack);
            int newChannel = Math.floorMod(channel + shift - 1, 8) + 1;
            if (channel != newChannel) {
                setChannel(stack, newChannel);
                if (displayChangeMessage) {
                    player.sendMessage(MekanismUtils.logFormat(AdditionsLang.CHANNEL_CHANGE.translate(newChannel)), Util.NIL_UUID);
                }
            }
        }
    }

    @Nonnull
    @Override
    public Component getScrollTextComponent(@Nonnull ItemStack stack) {
        return AdditionsLang.CHANNEL.translateColored(EnumColor.GRAY, getChannel(stack));
    }
}