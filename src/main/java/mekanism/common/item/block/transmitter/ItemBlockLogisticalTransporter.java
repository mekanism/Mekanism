package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockLogisticalTransporter extends ItemBlockTransporter<TileEntityLogisticalTransporter> {

    public ItemBlockLogisticalTransporter(BlockLargeTransmitter<TileEntityLogisticalTransporter> block, Item.Properties properties) {
        super(block, properties);
    }

    @NotNull
    @Override
    public TransporterTier getTier() {
        return Attribute.getTier(getBlock(), TransporterTier.class);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addStats(stack, context, tooltip, flag);
        TransporterTier tier = getTier();
        //Ensure no one somehow passes in invalid data
        float tickRate = Math.max(context.tickRate(), TickRateManager.MIN_TICKRATE);
        float speed = tier.getSpeed() / (5 * SharedConstants.TICKS_PER_SECOND / tickRate);
        float pull = tier.getPullAmount() * tickRate / MekanismUtils.TICKS_PER_HALF_SECOND;
        tooltip.add(MekanismLang.SPEED.translateColored(EnumColor.INDIGO, EnumColor.GRAY, UnitDisplayUtils.roundDecimals(speed)));
        tooltip.add(MekanismLang.PUMP_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, UnitDisplayUtils.roundDecimals(pull)));
    }
}