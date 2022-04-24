package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemUpgrade extends Item implements IUpgradeItem {

    private final Upgrade upgrade;

    public ItemUpgrade(Upgrade type, Properties properties) {
        super(properties.stacksTo(type.getMax()).rarity(Rarity.UNCOMMON));
        upgrade = type;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
            tooltip.add(getUpgradeType(stack).getDescription());
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getTranslatedKeyMessage()));
        }
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return upgrade;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level world = context.getLevel();
            BlockEntity tile = WorldUtils.getTileEntity(world, context.getClickedPos());
            if (tile instanceof IUpgradeTile upgradeTile) {
                if (upgradeTile.supportsUpgrades()) {
                    TileComponentUpgrade component = upgradeTile.getComponent();
                    ItemStack stack = context.getItemInHand();
                    Upgrade type = getUpgradeType(stack);
                    if (component.supports(type)) {
                        if (!world.isClientSide) {
                            int added = component.addUpgrades(type, stack.getCount());
                            if (added > 0) {
                                stack.shrink(added);
                            }
                        }
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}