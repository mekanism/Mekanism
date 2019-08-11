package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.EnergyDisplay;
import mekanism.common.util.TextComponentUtil.Translation;
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
        if (item instanceof ItemBlockInductionCell) {
            return ((ItemBlockInductionCell) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack itemstack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InductionCellTier tier = getTier(itemstack);
        if (tier != null) {
            tooltip.add(TextComponentUtil.build(tier.getBaseTier().getColor(), Translation.of("mekanism.tooltip.capacity"), ": ", EnumColor.GREY,
                  EnergyDisplay.of(tier.getMaxEnergy())));
            tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, Translation.of("mekanism.tooltip.storedEnergy"), ": ", EnumColor.GREY,
                  EnergyDisplay.of(getEnergy(itemstack))));
        }
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
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