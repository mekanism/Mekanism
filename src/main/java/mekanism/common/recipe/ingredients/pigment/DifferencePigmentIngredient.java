package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class DifferencePigmentIngredient extends DifferenceChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<DifferencePigmentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.basicPigment().codecNonEmpty().fieldOf(JsonConstants.BASE).forGetter(DifferencePigmentIngredient::base),
          IngredientCreatorAccess.basicPigment().codecNonEmpty().fieldOf(JsonConstants.SUBTRACTED).forGetter(DifferencePigmentIngredient::subtracted)
    ).apply(builder, DifferencePigmentIngredient::new));

    DifferencePigmentIngredient(IPigmentIngredient base, IPigmentIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferencePigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.DIFFERENCE.value();
    }
}
