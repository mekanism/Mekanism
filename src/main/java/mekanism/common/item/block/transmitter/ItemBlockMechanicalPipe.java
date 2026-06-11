package mekanism.common.item.block.transmitter;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.tier.PipeTier;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;

public class ItemBlockMechanicalPipe extends ItemBlockTooltip<BlockLargeTransmitter<TileEntityMechanicalPipe>> {

    private final PipeTier tier;

    public ItemBlockMechanicalPipe(BlockLargeTransmitter<TileEntityMechanicalPipe> block, Item.Properties properties) {
        tier = Attribute.getTierNN(block, PipeTier.class);
        super(block, true, properties);
    }

    @Override
    public PipeTier getTier() {
        return tier;
    }

    @Override
    protected void addDetails(ItemStack stack, ItemAccess itemAccess, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.addDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltipAdder.accept(MekanismLang.FLUIDS.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.FORGE));
    }

    @Override
    protected void addStats(ItemStack stack, ItemAccess itemAccess, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
          Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.addStats(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getCapacity())));
        tooltipAdder.accept(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getTransferRate())));
    }
}