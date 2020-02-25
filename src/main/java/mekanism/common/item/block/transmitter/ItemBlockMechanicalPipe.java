package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.tier.PipeTier;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockMechanicalPipe extends ItemBlockMultipartAble<BlockMechanicalPipe> implements ITieredItem<PipeTier> {

    public ItemBlockMechanicalPipe(BlockMechanicalPipe block) {
        super(block);
    }

    @Nullable
    @Override
    public PipeTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemBlockMechanicalPipe ? ((ItemBlockMechanicalPipe) item).getTier() : null;
    }

    @Nonnull
    @Override
    public PipeTier getTier() {
        return getBlock().getTier();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            PipeTier tier = getTier(stack);
            if (tier != null) {
                tooltip.add(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getPipeCapacity()));
                tooltip.add(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getPipePullAmount()));
            }
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
        } else {
            tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
            tooltip.add(MekanismLang.FLUIDS.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.FORGE));
        }
    }
}