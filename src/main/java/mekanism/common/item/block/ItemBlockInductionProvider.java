package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockInductionProvider extends ItemBlockTooltip<BlockInductionProvider> implements ITieredItem<InductionProviderTier> {

    public ItemBlockInductionProvider(BlockInductionProvider block) {
        super(block);
    }

    @Nullable
    @Override
    public InductionProviderTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockInductionProvider) {
            return ((ItemBlockInductionProvider) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InductionProviderTier tier = getTier(stack);
        if (tier != null) {
            tooltip.add(MekanismLang.INDUCTION_PORT_OUTPUT_RATE.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getOutput())));
        }
    }
}