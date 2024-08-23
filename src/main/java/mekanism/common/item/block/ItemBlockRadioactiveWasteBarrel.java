package mekanism.common.item.block;

import java.util.List;
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
import org.jetbrains.annotations.NotNull;

public class ItemBlockRadioactiveWasteBarrel extends ItemBlockTooltip<BlockRadioactiveWasteBarrel> {

    public ItemBlockRadioactiveWasteBarrel(BlockRadioactiveWasteBarrel block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    protected void addStats(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(MekanismConfig.general.radioactiveWasteBarrelMaxChemical.get())));
        int ticks = MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get();
        long decayAmount = MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get();
        if (decayAmount == 0 || ticks == 1) {
            tooltip.add(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount)));
        } else {
            //Show decay rate to four decimals with no trailing zeros (but without decimals if it divides evenly)
            tooltip.add(MekanismLang.WASTE_BARREL_DECAY_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                  TextUtils.format(UnitDisplayUtils.roundDecimals(decayAmount / (double) ticks, 4))));
            tooltip.add(MekanismLang.WASTE_BARREL_DECAY_RATE_ACTUAL.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(decayAmount),
                  EnumColor.GRAY, TextUtils.format(ticks)));
        }
    }
}