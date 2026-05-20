package mekanism.common.item.block.transmitter;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

public class ItemBlockPressurizedTube extends ItemBlockTooltip<BlockSmallTransmitter<TileEntityPressurizedTube>> {

    public ItemBlockPressurizedTube(BlockSmallTransmitter<TileEntityPressurizedTube> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @NotNull
    @Override
    public TubeTier getTier() {
        return Attribute.getTier(getBlock(), TubeTier.class);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addDetails(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltipAdder.accept(MekanismLang.CHEMICALS.translateColored(EnumColor.PURPLE, MekanismLang.MEKANISM));
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addStats(stack, context, tooltipDisplay, tooltipAdder, flag);
        TubeTier tier = getTier();
        tooltipAdder.accept(MekanismLang.CAPACITY_MB_PER_TICK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getCapacity())));
        tooltipAdder.accept(MekanismLang.PUMP_RATE_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(tier.getTransferRate())));
    }
}