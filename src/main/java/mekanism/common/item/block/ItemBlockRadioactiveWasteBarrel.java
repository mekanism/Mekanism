package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockRadioactiveWasteBarrel extends ItemBlockTooltip<BlockRadioactiveWasteBarrel> {

    public ItemBlockRadioactiveWasteBarrel(BlockRadioactiveWasteBarrel block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, EnumColor.GRAY, TextUtils.format(MekanismConfig.general.radioactiveWasteBarrelMaxGas.get())));
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