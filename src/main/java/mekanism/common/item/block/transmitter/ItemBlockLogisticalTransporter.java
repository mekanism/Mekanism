package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        int speed = tier.getSpeed();
        int pull = tier.getPullAmount();
        float tickRate = context.tickRate();
        if (tickRate > 0) {
            //TODO: Validate these calculations
            speed = (int) (speed / (100 / tickRate));
            pull = (int) (pull * tickRate / 10);
        } else {
            speed = 0;
            pull = 0;
        }
        tooltip.add(MekanismLang.SPEED.translateColored(EnumColor.INDIGO, EnumColor.GRAY, speed));
        tooltip.add(MekanismLang.PUMP_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, pull));
    }
}