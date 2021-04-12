package mekanism.common.integration.crafttweaker.jeitweaker;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
     * Gets what gas stacks are supposed to be hidden from JEI.
     */
    public static Collection<GasStack> getHiddenGasStacks() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.HIDDEN_GASES;
        }
        return Collections.emptySet();
    }

    /**
     * Gets the descriptions to add to various gases in JEI.
     */
    public static Map<GasStack, ITextComponent[]> getGasDescriptions() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.GAS_DESCRIPTIONS;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets what infusion stacks are supposed to be hidden from JEI.
     */
    public static Collection<InfusionStack> getHiddenInfusionStacks() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.HIDDEN_INFUSE_TYPES;
        }
        return Collections.emptySet();
    }

    /**
     * Gets the descriptions to add to various infuse types in JEI.
     */
    public static Map<InfusionStack, ITextComponent[]> getInfusionDescriptions() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.INFUSE_TYPE_DESCRIPTIONS;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets what pigment stacks are supposed to be hidden from JEI.
     */
    public static Collection<PigmentStack> getHiddenPigmentStacks() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.HIDDEN_PIGMENTS;
        }
        return Collections.emptySet();
    }

    /**
     * Gets the descriptions to add to various pigments in JEI.
     */
    public static Map<PigmentStack, ITextComponent[]> getPigmentDescriptions() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.PIGMENT_DESCRIPTIONS;
        }
        return Collections.emptyMap();
    }

    /**
     * Gets what slurry stacks are supposed to be hidden from JEI.
     */
    public static Collection<SlurryStack> getHiddenSlurryStacks() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.HIDDEN_SLURRIES;
        }
        return Collections.emptySet();
    }

    /**
     * Gets the descriptions to add to various slurries in JEI.
     */
    public static Map<SlurryStack, ITextComponent[]> getSlurryDescriptions() {
        if (Mekanism.hooks.JEITweakerLoaded) {
            return JEITweakerExpansion.SLURRY_DESCRIPTIONS;
        }
        return Collections.emptyMap();
    }
}