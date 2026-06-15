package mekanism.common.item;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.lib.radiation.RadiationUtil;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ItemDosimeter extends Item {

    public ItemDosimeter(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        } else if (!world.isClientSide()) {
            sendDosimeterLevel(player, player, MekanismLang.RADIATION_EXPOSURE);
            CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        } else if (!player.level().isClientSide()) {
            sendDosimeterLevel(entity, player, MekanismLang.RADIATION_EXPOSURE_ENTITY);
        }
        return InteractionResult.SUCCESS;
    }

    private void sendDosimeterLevel(LivingEntity entity, Player player, ILangEntry doseLangEntry) {
        double radiation = RadiationManager.isGlobalRadiationEnabled() ? entity.getData(MekanismAttachmentTypes.RADIATION) : 0;
        EnumColor severityColor = RadiationScale.getSeverityColor(radiation);
        player.sendSystemMessage(doseLangEntry.translateColored(EnumColor.GRAY, severityColor, UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 3)));
        if (MekanismConfig.common.enableDecayTimers.get() && radiation > IRadiationManager.INSTANCE.minRadiationMagnitude()) {
            player.sendSystemMessage(MekanismLang.RADIATION_DECAY_TIME.translateColored(EnumColor.GRAY, severityColor,
                  TextUtils.getHoursMinutes(player.level(), RadiationUtil.getDecayTime(radiation, false))));
        }
    }
}