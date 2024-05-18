package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class DifferencePigmentIngredient extends DifferenceChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<DifferencePigmentIngredient> CODEC = codec(IngredientCreatorAccess.pigment(), DifferencePigmentIngredient::new);

    DifferencePigmentIngredient(IPigmentIngredient base, IPigmentIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferencePigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.DIFFERENCE.value();
    }
}
