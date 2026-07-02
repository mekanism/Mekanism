package mekanism.common.item;

import java.util.function.Consumer;
import mekanism.api.Upgrade;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemUpgrade extends Item implements IUpgradeItem {

    private final Upgrade upgrade;

    public ItemUpgrade(Upgrade type, Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
        upgrade = type;
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            Upgrade upgradeType = getUpgradeType();
            tooltipAdder.accept(upgradeType.getDescription());
            tooltipAdder.accept(APILang.UPGRADE_MAX_INSTALLED.translate(upgradeType.getMax()));
        } else {
            tooltipAdder.accept(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public Upgrade getUpgradeType() {
        return upgrade;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level world = context.getLevel();
            BlockEntity tile = WorldUtils.getTileEntity(world, context.getClickedPos());
            if (tile instanceof IUpgradeTile upgradeTile && upgradeTile.supportsUpgrades()) {
                ItemStack stack = context.getItemInHand();
                Upgrade type = getUpgradeType();
                if (upgradeTile.supportsUpgrade(type)) {
                    if (!world.isClientSide()) {
                        int added = upgradeTile.addUpgrades(type, stack.count());
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
}