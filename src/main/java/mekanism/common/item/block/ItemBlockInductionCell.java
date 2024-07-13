package mekanism.common.item.block;

import java.util.List;
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
import org.jetbrains.annotations.NotNull;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {

    public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block, Item.Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public InductionCellTier getTier() {
        return Attribute.getTier(getBlock(), InductionCellTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        InductionCellTier tier = getTier();
        tooltip.add(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())));
        StorageUtils.addStoredEnergy(stack, tooltip, false);
    }

    @Override
    protected boolean exposesEnergyCapOrTooltips() {
        return false;
    }
}