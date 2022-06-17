package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ItemGeigerCounter extends Item {

    public ItemGeigerCounter(Properties props) {
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                double magnitude = MekanismAPI.getRadiationManager().getRadiationLevel(player);
                player.sendSystemMessage(MekanismLang.RADIATION_EXPOSURE.translateColored(EnumColor.GRAY,
                      RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)));
                CriteriaTriggers.USING_ITEM.trigger((ServerPlayer) player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }
}
