package mekanism.common.integration.crafttweaker.jeitweaker;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.Mekanism;
import net.minecraft.util.text.ITextComponent;

/**
 * Helper so that if JEITweaker is not loaded we don't accidentally cause any class loading issues.
 */
public class JEITweakerHelper {

    /**
     * Removes chemical stacks that are supposed to be hidden from JEI.
     */
    public static void removeHiddenStacks(Consumer<Collection<GasStack>> gasRemover, Consumer<Collection<InfusionStack>> infusionRemover,
          Consumer<Collection<PigmentStack>> pigmentRemover, Consumer<Collection<SlurryStack>> slurryRemover) {
        if (Mekanism.hooks.JEITweakerLoaded) {
            removeIfNotEmpty(JEITweakerExpansion.HIDDEN_GASES, gasRemover);
            removeIfNotEmpty(JEITweakerExpansion.HIDDEN_INFUSE_TYPES, infusionRemover);
            removeIfNotEmpty(JEITweakerExpansion.HIDDEN_PIGMENTS, pigmentRemover);
            removeIfNotEmpty(JEITweakerExpansion.HIDDEN_SLURRIES, slurryRemover);
        }
    }

    /**
     * Helper to remove via the consumer if the collection is not empty
     */
    private static <T> void removeIfNotEmpty(Collection<T> hidden, Consumer<Collection<T>> remover) {
        if (!hidden.isEmpty()) {
            remover.accept(hidden);
        }
    }

    /**
     * Adds the descriptions to add to various chemicals in JEI.
     */
    public static void addDescriptions(BiConsumer<GasStack, ITextComponent[]> gasDescriptionAdder, BiConsumer<InfusionStack, ITextComponent[]> infusionDescriptionAdder,
          BiConsumer<PigmentStack, ITextComponent[]> pigmentDescriptionAdder, BiConsumer<SlurryStack, ITextComponent[]> slurryDescriptionAdder) {
        if (Mekanism.hooks.JEITweakerLoaded) {
            JEITweakerExpansion.GAS_DESCRIPTIONS.forEach(gasDescriptionAdder);
            JEITweakerExpansion.INFUSE_TYPE_DESCRIPTIONS.forEach(infusionDescriptionAdder);
            JEITweakerExpansion.PIGMENT_DESCRIPTIONS.forEach(pigmentDescriptionAdder);
            JEITweakerExpansion.SLURRY_DESCRIPTIONS.forEach(slurryDescriptionAdder);
        }
    }
}