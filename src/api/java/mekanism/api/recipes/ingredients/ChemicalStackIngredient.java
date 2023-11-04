package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import mekanism.api.IMekanismAccess;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import org.jetbrains.annotations.NotNull;

/**
 * Base implementation for how Mekanism handle's ChemicalStack Ingredients.
 */
public interface ChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> extends InputIngredient<@NotNull STACK> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param chemical Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@NotNull CHEMICAL chemical);

    /**
     * Base implementation for how Mekanism handle's GasStack Ingredients.
     * <p>
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#gas()}.
     */
    interface GasStackIngredient extends ChemicalStackIngredient<Gas, GasStack> {
        @Override
        @Deprecated(forRemoval = true)
        default JsonElement serialize() {
            return IMekanismAccess.INSTANCE.gasStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
        }
    }

    /**
     * Base implementation for how Mekanism handle's InfusionStack Ingredients.
     * <p>
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#infusion()}.
     */
    interface InfusionStackIngredient extends ChemicalStackIngredient<InfuseType, InfusionStack> {
        @Override
        @Deprecated(forRemoval = true)
        default JsonElement serialize() {
            return IMekanismAccess.INSTANCE.infusionStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
        }
    }

    /**
     * Base implementation for how Mekanism handle's PigmentStack Ingredients.
     * <p>
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#pigment()}.
     */
    interface PigmentStackIngredient extends ChemicalStackIngredient<Pigment, PigmentStack> {
        @Override
        @Deprecated(forRemoval = true)
        default JsonElement serialize() {
            return IMekanismAccess.INSTANCE.pigmentStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
        }
    }

    /**
     * Base implementation for how Mekanism handle's SlurryStack Ingredients.
     * <p>
     * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#slurry()}.
     */
    interface SlurryStackIngredient extends ChemicalStackIngredient<Slurry, SlurryStack> {
        @Override
        @Deprecated(forRemoval = true)
        default JsonElement serialize() {
            return IMekanismAccess.INSTANCE.slurryStackIngredientCreator().codec().encodeStart(JsonOps.INSTANCE, this).getOrThrow(false, e->{});
        }
    }
}