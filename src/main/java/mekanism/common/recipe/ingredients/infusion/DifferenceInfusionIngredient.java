package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismInfusionIngredientTypes;

@NothingNullByDefault
public final class DifferenceInfusionIngredient extends DifferenceChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final MapCodec<DifferenceInfusionIngredient> CODEC = codec(IngredientCreatorAccess.infusion(), DifferenceInfusionIngredient::new);

    DifferenceInfusionIngredient(IInfusionIngredient base, IInfusionIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferenceInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.DIFFERENCE.value();
    }
}
