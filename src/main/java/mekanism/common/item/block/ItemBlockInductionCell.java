package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {

    public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block) {
        super(block, ItemDeferredRegister.getMekBaseProperties());
    }

    @Nonnull
    @Override
    public InductionCellTier getTier() {
        return Attribute.getTier(getBlock(), InductionCellTier.class);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        InductionCellTier tier = getTier();
        tooltip.add(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())));
        tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(StorageUtils.getStoredEnergyFromNBT(stack),
              tier.getMaxEnergy())));
    }
}