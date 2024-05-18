package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismGasIngredientTypes;

@NothingNullByDefault
public final class CompoundGasIngredient extends CompoundChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<CompoundGasIngredient> CODEC = codec(IngredientCreatorAccess.gas(), CompoundGasIngredient::new);

    CompoundGasIngredient(List<IGasIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundGasIngredient> codec() {
        return MekanismGasIngredientTypes.COMPOUND.value();
    }
}
