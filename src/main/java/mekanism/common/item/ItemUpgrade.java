package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemUpgrade extends Item implements IUpgradeItem {

    private Upgrade upgrade;

    public ItemUpgrade(Upgrade type, Properties properties) {
        super(properties.maxStackSize(type.getMax()));
        upgrade = type;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(getUpgradeType(stack).getDescription());
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
        }
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return upgrade;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            World world = context.getWorld();
            TileEntity tile = MekanismUtils.getTileEntity(world, context.getPos());
            ItemStack stack = player.getHeldItem(context.getHand());
            Upgrade type = getUpgradeType(stack);
            if (tile instanceof IUpgradeTile) {
                IUpgradeTile upgradeTile = (IUpgradeTile) tile;
                if (!upgradeTile.supportsUpgrades()) {
                    //Can't support upgrades so continue on
                    return ActionResultType.PASS;
                }
                TileComponentUpgrade component = upgradeTile.getComponent();
                if (component.supports(type)) {
                    if (!world.isRemote && component.getUpgrades(type) < type.getMax()) {
                        component.addUpgrade(type);
                        stack.shrink(1);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }
}