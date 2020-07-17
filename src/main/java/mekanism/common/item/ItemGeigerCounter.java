package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public class ItemGeigerCounter extends Item {

    public ItemGeigerCounter(Properties props) {
        super(props.maxStackSize(1).rarity(Rarity.UNCOMMON));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking() && !world.isRemote()) {
            double magnitude = Mekanism.radiationManager.getRadiationLevel(player);
            player.sendMessage(MekanismLang.RADIATION_EXPOSURE.translateColored(EnumColor.GRAY,
                  RadiationScale.getSeverityColor(magnitude), UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SVH, 3)), Util.DUMMY_UUID);
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }
}
