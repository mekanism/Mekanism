package mekanism.common.item.block.transmitter;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.block.transmitter.BlockLargeTransmitter;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockTransporter<TILE extends TileEntityLogisticalTransporterBase> extends ItemBlockTooltip<BlockLargeTransmitter<TILE>> {

    @Nullable
    private final ILangEntry extraDetails;

    public ItemBlockTransporter(BlockLargeTransmitter<TILE> block, Item.Properties properties) {
        this(block, properties, null);
    }

    public ItemBlockTransporter(BlockLargeTransmitter<TILE> block, Item.Properties properties, @Nullable ILangEntry extraDetails) {
        super(block, true, properties);
        this.extraDetails = extraDetails;
    }

    @Override
    protected void addDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addDetails(stack, context, tooltip, flag);
        tooltip.add(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltip.add(MekanismLang.ITEMS.translateColored(EnumColor.PURPLE, MekanismLang.UNIVERSAL));
        tooltip.add(MekanismLang.BLOCKS.translateColored(EnumColor.PURPLE, MekanismLang.UNIVERSAL));
        if (extraDetails != null) {
            tooltip.add(extraDetails.translateColored(EnumColor.DARK_RED));
        }
    }
}