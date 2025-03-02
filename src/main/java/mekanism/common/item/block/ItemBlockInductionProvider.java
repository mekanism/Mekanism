package mekanism.common.item.block;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockInductionProvider extends ItemBlockTooltip<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>> {

    public ItemBlockInductionProvider(BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>> block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public InductionProviderTier getTier() {
        return Attribute.getTier(getBlock(), InductionProviderTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        InductionProviderTier tier = getTier();
        tooltip.add(MekanismLang.INDUCTION_PORT_OUTPUT_RATE.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getOutput())));
    }
}