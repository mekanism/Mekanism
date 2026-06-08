package mekanism.common.item.block.transmitter;

import java.util.function.Consumer;
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
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;
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
    protected void addDetails(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.addDetails(stack, itemAccess, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(MekanismLang.CAPABLE_OF_TRANSFERRING.translateColored(EnumColor.DARK_GRAY));
        tooltipAdder.accept(MekanismLang.ITEMS.translateColored(EnumColor.PURPLE, MekanismLang.UNIVERSAL));
        tooltipAdder.accept(MekanismLang.BLOCKS.translateColored(EnumColor.PURPLE, MekanismLang.UNIVERSAL));
        if (extraDetails != null) {
            tooltipAdder.accept(extraDetails.translateColored(EnumColor.DARK_RED));
        }
    }
}