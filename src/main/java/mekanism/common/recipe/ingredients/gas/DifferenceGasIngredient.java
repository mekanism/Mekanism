package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismGasIngredientTypes;

@NothingNullByDefault
public final class DifferenceGasIngredient extends DifferenceChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<DifferenceGasIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.basicGas().codecNonEmpty().fieldOf(JsonConstants.BASE).forGetter(DifferenceGasIngredient::base),
          IngredientCreatorAccess.basicGas().codecNonEmpty().fieldOf(JsonConstants.SUBTRACTED).forGetter(DifferenceGasIngredient::subtracted)
    ).apply(builder, DifferenceGasIngredient::new));

    DifferenceGasIngredient(IGasIngredient base, IGasIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferenceGasIngredient> codec() {
        return MekanismGasIngredientTypes.DIFFERENCE.value();
    }
}
