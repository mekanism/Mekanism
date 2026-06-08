package mekanism.common.lib.frequency;

import mekanism.common.attachments.FrequencyAware;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public interface IFrequencyItem {

    FrequencyType<?> getFrequencyType();

    default void pruneInvalidTrusted(ItemAccess itemAccess, TransactionContext transaction) {
        pruneInvalidTrusted(itemAccess, transaction, MekanismDataComponents.getFrequencyComponent(getFrequencyType()));
    }

    private <FREQ extends Frequency> void pruneInvalidTrusted(ItemAccess itemAccess, TransactionContext transaction, @Nullable DataComponentType<? extends FrequencyAware<FREQ>> frequencyComponent) {
        if (frequencyComponent != null) {
            FrequencyAware<FREQ> frequencyAware = itemAccess.getResource().get(frequencyComponent);
            if (frequencyAware != null) {
                frequencyAware.pruneInvalidTrusted(itemAccess, frequencyComponent, transaction);
            }
        }
    }
}