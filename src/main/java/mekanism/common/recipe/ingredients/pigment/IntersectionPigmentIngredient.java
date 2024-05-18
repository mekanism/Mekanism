package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class IntersectionPigmentIngredient extends IntersectionChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<IntersectionPigmentIngredient> CODEC = codec(IngredientCreatorAccess.pigment(), IntersectionPigmentIngredient::new);

    IntersectionPigmentIngredient(List<IPigmentIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<IntersectionPigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.INTERSECTION.value();
    }
}
