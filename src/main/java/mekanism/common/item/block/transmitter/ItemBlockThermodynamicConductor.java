package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockThermodynamicConductor extends ItemBlockTooltip<BlockSmallTransmitter<TileEntityThermodynamicConductor>> {

    public ItemBlockThermodynamicConductor(BlockSmallTransmitter<TileEntityThermodynamicConductor> block, Item.Properties properties) {
        super(block, true, properties);
    }

    @NotNull
    @Override
    public ConductorTier getTier() {
        return Attribute.getTier(getBlock(), ConductorTier.class);
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addDetails(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltip.add(MekanismLang.HEAT.translateColored(EnumColor.PURPLE, MekanismLang.MEKANISM));
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addStats(stack, context, tooltip, flag);
        ConductorTier tier = getTier();
        tooltip.add(MekanismLang.CONDUCTION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getInverseConduction()));
        tooltip.add(MekanismLang.INSULATION.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getInverseConductionInsulation()));
        tooltip.add(MekanismLang.HEAT_CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, tier.getHeatCapacity()));
    }
}