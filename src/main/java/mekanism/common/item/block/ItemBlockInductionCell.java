package mekanism.common.item.block;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {

    private final InductionCellTier tier;

    public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block, Item.Properties properties) {
        tier = Attribute.getTierNN(block, InductionCellTier.class);
        super(block, properties);
    }

    @Override
    public InductionCellTier getTier() {
        return tier;
    }

    @Override
    protected void addStats(ItemStack stack, ItemAccess itemAccess, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getTextColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())));
        StorageUtils.addStoredEnergy(itemAccess, tooltipAdder, false);
    }

    @Override
    protected boolean exposesEnergyCapOrTooltips() {
        return false;
    }
}