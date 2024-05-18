package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.common.registries.MekanismGasIngredientTypes;

@NothingNullByDefault
public final class EmptyGasIngredient extends EmptyChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final EmptyGasIngredient INSTANCE = new EmptyGasIngredient();
    public static final MapCodec<EmptyGasIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyGasIngredient() {
    }

    @Override
    public MapCodec<EmptyGasIngredient> codec() {
        return MekanismGasIngredientTypes.EMPTY.value();
    }
}
