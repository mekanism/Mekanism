package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.DifferenceChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class DifferenceSlurryIngredient extends DifferenceChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<DifferenceSlurryIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.slurry().codecNonEmpty().fieldOf(JsonConstants.BASE).forGetter(DifferenceSlurryIngredient::base),
          IngredientCreatorAccess.slurry().codecNonEmpty().fieldOf(JsonConstants.SUBTRACTED).forGetter(DifferenceSlurryIngredient::subtracted)
    ).apply(builder, DifferenceSlurryIngredient::new));

    DifferenceSlurryIngredient(ISlurryIngredient base, ISlurryIngredient subtracted) {
        super(base, subtracted);
    }

    @Override
    public MapCodec<DifferenceSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.DIFFERENCE.value();
    }
}
