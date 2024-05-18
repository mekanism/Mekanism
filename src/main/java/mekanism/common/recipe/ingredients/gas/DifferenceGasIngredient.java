package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismGasIngredientTypes;

@NothingNullByDefault
public final class DifferenceGasIngredient extends DifferenceChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<DifferenceGasIngredient> CODEC = codec(IngredientCreatorAccess.gas(), DifferenceGasIngredient::new);

    DifferenceGasIngredient(IGasIngredient base, IGasIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferenceGasIngredient> codec() {
        return MekanismGasIngredientTypes.DIFFERENCE.value();
    }
}
