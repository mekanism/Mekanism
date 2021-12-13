package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemPortableTeleporter extends ItemEnergized implements IFrequencyItem {

    public ItemPortableTeleporter(Properties properties) {
        super(MekanismConfig.gear.portableTeleporterChargeRate, MekanismConfig.gear.portableTeleporterMaxEnergy, properties.rarity(Rarity.RARE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(OwnerDisplay.of(Minecraft.getInstance().player, getOwnerUUID(stack)).getTextComponent());
        MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> use(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getOwnerUUID(stack) == null) {
            if (!world.isClientSide) {
                SecurityUtils.claimItem(player, stack);
            }
        } else if (SecurityUtils.canAccess(player, stack)) {
            if (!world.isClientSide) {
                MekanismContainerTypes.PORTABLE_TELEPORTER.tryOpenGui((ServerPlayerEntity) player, hand, stack);
            }
        } else {
            if (!world.isClientSide) {
                SecurityUtils.displayNoAccess(player);
            }
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}