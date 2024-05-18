package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class IntersectionSlurryIngredient extends IntersectionChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<IntersectionSlurryIngredient> CODEC = codec(IngredientCreatorAccess.slurry(), IntersectionSlurryIngredient::new);

    IntersectionSlurryIngredient(List<ISlurryIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<IntersectionSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.INTERSECTION.value();
    }
}
