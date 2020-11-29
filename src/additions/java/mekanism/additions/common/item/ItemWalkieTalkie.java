package mekanism.additions.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.config.MekanismAdditionsConfig;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemWalkieTalkie extends Item implements IModeItem {

    public ItemWalkieTalkie(Item.Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OnOff.of(getOn(stack), true).getTextComponent());
        tooltip.add(AdditionsLang.CHANNEL.translateColored(EnumColor.DARK_AQUA, EnumColor.GRAY, getChannel(stack)));
        if (!MekanismAdditionsConfig.additions.voiceServerEnabled.get()) {
            tooltip.add(AdditionsLang.WALKIE_DISABLED.translateColored(EnumColor.DARK_RED));
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            setOn(itemStack, !getOn(itemStack));
            return new ActionResult<>(ActionResultType.SUCCESS, itemStack);
        }
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemsEqual(oldStack, newStack);
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
    public void changeMode(@Nonnull PlayerEntity player, @Nonnull ItemStack stack, int shift, boolean displayChangeMessage) {
        if (getOn(stack)) {
            int channel = getChannel(stack);
            int newChannel = Math.floorMod(channel + shift - 1, 8) + 1;
            if (channel != newChannel) {
                setChannel(stack, newChannel);
                if (displayChangeMessage) {
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY,
                          AdditionsLang.CHANNEL_CHANGE.translate(newChannel)), Util.DUMMY_UUID);
                }
            }
        }
    }

    @Nonnull
    @Override
    public ITextComponent getScrollTextComponent(@Nonnull ItemStack stack) {
        return AdditionsLang.CHANNEL.translateColored(EnumColor.GRAY, getChannel(stack));
    }
}