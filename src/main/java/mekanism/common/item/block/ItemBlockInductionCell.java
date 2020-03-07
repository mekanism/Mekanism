package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Should this implement IItemEnergized instead of IEnergizedItem
public class ItemBlockInductionCell extends ItemBlockTooltip<BlockInductionCell> implements IEnergizedItem, ITieredItem<InductionCellTier> {

    public ItemBlockInductionCell(BlockInductionCell block) {
        super(block);
    }

    @Nullable
    @Override
    public InductionCellTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemBlockInductionCell ? ((ItemBlockInductionCell) item).getTier() : null;
    }

    @Nonnull
    @Override
    public InductionCellTier getTier() {
        return getBlock().getTier();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InductionCellTier tier = getTier(stack);
        if (tier != null) {
            tooltip.add(MekanismLang.CAPACITY.translateColored(tier.getBaseTier().getColor(), EnumColor.GRAY, EnergyDisplay.of(tier.getMaxEnergy())));
            tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(getEnergy(stack), getMaxEnergy(stack))));
        }
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        return ItemDataUtils.getDouble(itemStack, NBTConstants.ENERGY_STORED);
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        ItemDataUtils.setDouble(itemStack, NBTConstants.ENERGY_STORED, Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        InductionCellTier tier = getTier(itemStack);
        return tier == null ? 0 : tier.getMaxEnergy();
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }
}