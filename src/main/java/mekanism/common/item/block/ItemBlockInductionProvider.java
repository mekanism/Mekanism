package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.machine.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityInductionProvider;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockInductionProvider extends ItemBlockTooltip<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>> {

    public ItemBlockInductionProvider(BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>> block) {
        super(block);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InductionProviderTier tier = Attribute.getTier(getBlock(), InductionProviderTier.class);
        if (tier != null) {
            tooltip.add(MekanismLang.INDUCTION_PORT_OUTPUT_RATE.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getOutput())));
        }
    }
}