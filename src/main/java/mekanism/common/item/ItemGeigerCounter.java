package mekanism.common.item;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.LevelAndMaxMagnitude;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.lib.radiation.RadiationUtil;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemGeigerCounter extends Item {

    public ItemGeigerCounter(Properties props) {
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                LevelAndMaxMagnitude levelAndMaxMagnitude = RadiationManager.get().getRadiationLevelAndMaxMagnitude(player);
                double magnitude = levelAndMaxMagnitude.level();
                EnumColor severityColor = RadiationScale.getSeverityColor(magnitude);
                player.sendSystemMessage(MekanismLang.RADIATION_EXPOSURE.translateColored(EnumColor.GRAY, severityColor,
                      UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)));
                if (MekanismConfig.common.enableDecayTimers.get() && magnitude > IRadiationManager.INSTANCE.baselineRadiation()) {
                    player.sendSystemMessage(MekanismLang.RADIATION_DECAY_TIME.translateColored(EnumColor.GRAY,
                          severityColor, TextUtils.getHoursMinutes(world, RadiationUtil.getDecayTime(levelAndMaxMagnitude.maxMagnitude(), true))));
                }
                CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }
}
