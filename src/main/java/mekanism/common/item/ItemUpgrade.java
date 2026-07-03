package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.MekanismRegistries;
import mekanism.common.component.UpgradeType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister.ICustomCreativeTabContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemUpgrade extends Item implements ICustomCreativeTabContents {

    public ItemUpgrade(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        stack.addToTooltip(MekanismDataComponents.UPGRADE_TYPE.get(), context, tooltipDisplay, tooltipAdder, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level world = context.getLevel();
            BlockEntity tile = WorldUtils.getTileEntity(world, context.getClickedPos());
            if (tile instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrades()) {
                ItemStack stack = context.getItemInHand();
                UpgradeType upgradeType = stack.get(MekanismDataComponents.UPGRADE_TYPE);
                if (upgradeType != null && upgradeTile.supportsUpgrade(upgradeType.type())) {
                    if (!world.isClientSide()) {
                        int added = upgradeTile.addUpgrades(world.registryAccess(), upgradeType.type(), stack.count());
                        if (added > 0) {
                            stack.consume(added, player);
                        }
                    }
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> addToTab) {
        displayParameters.holders().lookupOrThrow(MekanismRegistries.Keys.UPGRADES)
              .listElements()
              .map(UpgradeUtils::getStack)
              .forEach(addToTab);
    }

    @Override
    public boolean addDefault() {
        return false;
    }
}