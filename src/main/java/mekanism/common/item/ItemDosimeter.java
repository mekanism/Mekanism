package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
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

public class ItemDosimeter extends Item {

    public ItemDosimeter(Properties properties) {
        super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking() && !world.isRemote()) {
            player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c ->
                  player.sendMessage(MekanismLang.RADIATION_DOSE.translateColored(EnumColor.GRAY, RadiationScale.getSeverityColor(c.getRadiation()),
                        UnitDisplayUtils.getDisplayShort(c.getRadiation(), RadiationUnit.SV, 3)), Util.DUMMY_UUID));
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }
}
