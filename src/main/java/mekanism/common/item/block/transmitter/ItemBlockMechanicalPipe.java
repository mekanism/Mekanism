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
import org.jetbrains.annotations.NotNull;

public class ItemBlockMechanicalPipe extends ItemBlockTooltip<BlockLargeTransmitter<TileEntityMechanicalPipe>> {

    public ItemBlockMechanicalPipe(BlockLargeTransmitter<TileEntityMechanicalPipe> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @NotNull
    @Override
    public PipeTier getTier() {
        return Attribute.getTier(getBlock(), PipeTier.class);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltipAdder.accept(MekanismLang.FLUIDS.translateColored(EnumColor.PURPLE, EnumColor.GRAY, MekanismLang.FORGE));
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addStats(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        PipeTier tier = getTier();
        tooltipAdder.accept(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getCapacity())));
        tooltipAdder.accept(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getTransferRate())));
    }
}