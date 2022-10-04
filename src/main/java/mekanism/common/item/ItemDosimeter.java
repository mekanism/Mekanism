package mekanism.common.item;

import mekanism.api.MekanismAPI;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemDosimeter extends Item {

    public ItemDosimeter(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(cap -> {
                    sendDosimeterLevel(cap, player, MekanismLang.RADIATION_EXPOSURE);
                    CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
                });
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                entity.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(cap -> sendDosimeterLevel(cap, player, MekanismLang.RADIATION_EXPOSURE_ENTITY));
            }
            return InteractionResult.sidedSuccess(player.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private void sendDosimeterLevel(IRadiationEntity cap, Player player, ILangEntry doseLangEntry) {
        double radiation = MekanismAPI.getRadiationManager().isRadiationEnabled() ? cap.getRadiation() : 0;
        EnumColor severityColor = RadiationScale.getSeverityColor(radiation);
        player.sendSystemMessage(doseLangEntry.translateColored(EnumColor.GRAY, severityColor, UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 3)));
        if (MekanismConfig.common.enableDecayTimers.get() && radiation > RadiationManager.MIN_MAGNITUDE) {
            player.sendSystemMessage(MekanismLang.RADIATION_DECAY_TIME.translateColored(EnumColor.GRAY, severityColor,
                  TextUtils.getHoursMinutes(RadiationManager.INSTANCE.getDecayTime(radiation, false))));
        }
    }
}