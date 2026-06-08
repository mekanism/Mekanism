package mekanism.common.item.block;

import java.util.Objects;
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
import org.jetbrains.annotations.NotNull;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {

    private final InductionCellTier tier;

    public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block, Item.Properties properties) {
        tier = Objects.requireNonNull(Attribute.getTier(block, InductionCellTier.class));
        super(block, properties);
    }

    @NotNull
    @Override
    public InductionCellTier getTier() {
        return tier;
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        tooltipAdder.accept(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())));
        StorageUtils.addStoredEnergy(itemAccess, tooltipAdder, false);
    }

    @Override
    protected boolean exposesEnergyCapOrTooltips() {
        return false;
    }
}