package mekanism.common.item.interfaces;

import mekanism.common.attachments.FrequencyAware;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IColoredItem {

    @Nullable
    default DataComponentType<? extends FrequencyAware<?>> getFrequencyComponent() {
        return null;
    }

    @SuppressWarnings("unchecked")
    default <FREQ extends Frequency> void syncColorWithFrequency(ItemStack stack) {
        DataComponentType<FrequencyAware<FREQ>> frequencyComponent = (DataComponentType<FrequencyAware<FREQ>>) getFrequencyComponent();
        if (frequencyComponent != null) {
            FrequencyAware<FREQ> frequencyAware = stack.getOrDefault(frequencyComponent, FrequencyAware.none());
            if (frequencyAware.getFrequency(stack, frequencyComponent) instanceof IColorableFrequency frequency) {
                stack.set(MekanismDataComponents.COLOR, frequency.getColor());
            } else {
                stack.remove(MekanismDataComponents.COLOR);
            }
        }
    }
}