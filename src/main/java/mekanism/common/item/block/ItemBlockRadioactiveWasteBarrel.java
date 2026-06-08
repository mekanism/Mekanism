package mekanism.common.item.block;

import java.util.function.Consumer;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.NotNull;

public class ItemBlockRadioactiveWasteBarrel extends ItemBlockTooltip<BlockRadioactiveWasteBarrel> {

    public ItemBlockRadioactiveWasteBarrel(BlockRadioactiveWasteBarrel block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull ItemAccess itemAccess, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay tooltipDisplay,
          @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        tooltipAdder.accept(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(MekanismConfig.general.radioactiveWasteBarrelMaxChemical.get())));
        int ticks = MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get();
        int decayAmount = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get();
        if (decayAmount == 0 || ticks == 1) {
            tooltipAdder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount)));
        } else {
            //Show decay rate to four decimals with no trailing zeros (but without decimals if it divides evenly)
            tooltipAdder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                  TextUtils.format(UnitDisplayUtils.roundDecimals(decayAmount / (double) ticks, 4))));
            tooltipAdder.accept(MekanismLang.WASTE_BARREL_DECAY_RATE_ACTUAL.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount),
                  EnumColor.GRAY, TextUtils.format(ticks)));
        }
    }
}