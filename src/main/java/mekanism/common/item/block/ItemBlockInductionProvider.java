package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockInductionProvider extends ItemBlockTooltip<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>> {

    public ItemBlockInductionProvider(BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>> block) {
        super(block, ItemDeferredRegister.getMekBaseProperties());
    }

    @Override
    @Nonnull
    public InductionProviderTier getTier() {
        return Attribute.getTier(getBlock(), InductionProviderTier.class);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        InductionProviderTier tier = getTier();
        tooltip.add(MekanismLang.INDUCTION_PORT_OUTPUT_RATE.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getOutput())));
    }
}