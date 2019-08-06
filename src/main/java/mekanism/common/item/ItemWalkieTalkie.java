package mekanism.common.item;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.IItemNetwork;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemWalkieTalkie extends ItemMekanism implements IItemNetwork {

    public static ModelResourceLocation OFF_MODEL = new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "WalkieTalkie"), "inventory");

    public static Map<Integer, ModelResourceLocation> CHANNEL_MODELS = new HashMap<>();

    public ItemWalkieTalkie() {
        super("walkie_talkie");
        setMaxStackSize(1);
    }

    public static ModelResourceLocation getModel(int channel) {
        CHANNEL_MODELS.computeIfAbsent(channel, c -> new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "WalkieTalkie_ch" + c), "inventory"));
        return CHANNEL_MODELS.get(channel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add((getOn(itemstack) ? EnumColor.DARK_GREEN : EnumColor.DARK_RED) + LangUtils.localize("gui." + (getOn(itemstack) ? "on" : "off")));
        list.add(EnumColor.DARK_AQUA + LangUtils.localize("tooltip.channel") + ": " + EnumColor.GREY + getChannel(itemstack));
        if (!MekanismConfig.current().general.voiceServerEnabled.val()) {
            list.add(EnumColor.DARK_RED + LangUtils.localize("tooltip.walkie_disabled"));
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
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
        ItemDataUtils.setBoolean(itemStack, "on", on);
    }

    public boolean getOn(ItemStack itemStack) {
        return ItemDataUtils.getBoolean(itemStack, "on");
    }

    public void setChannel(ItemStack itemStack, int channel) {
        ItemDataUtils.setInt(itemStack, "channel", channel);
    }

    public int getChannel(ItemStack itemStack) {
        int channel = ItemDataUtils.getInt(itemStack, "channel");
        if (channel == 0) {
            setChannel(itemStack, 1);
            channel = 1;
        }
        return channel;
    }

    @Override
    public void handlePacketData(ItemStack stack, ByteBuf dataStream) {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int channel = dataStream.readInt();
            setChannel(stack, channel);
        }
    }
}