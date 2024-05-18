package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class DifferenceSlurryIngredient extends DifferenceChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<DifferenceSlurryIngredient> CODEC = codec(IngredientCreatorAccess.slurry(), DifferenceSlurryIngredient::new);

    DifferenceSlurryIngredient(ISlurryIngredient base, ISlurryIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferenceSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.DIFFERENCE.value();
    }
}
