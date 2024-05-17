package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class IntersectionPigmentIngredient extends IntersectionChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<IntersectionPigmentIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.basicPigment().listCodecMultipleElements().fieldOf(JsonConstants.CHILDREN).forGetter(IntersectionPigmentIngredient::children)
    ).apply(builder, IntersectionPigmentIngredient::new));

    IntersectionPigmentIngredient(List<IPigmentIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<IntersectionPigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.INTERSECTION.value();
    }
}
