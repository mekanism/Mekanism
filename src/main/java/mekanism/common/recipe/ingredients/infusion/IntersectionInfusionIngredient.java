package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismInfusionIngredientTypes;

@NothingNullByDefault
public final class IntersectionInfusionIngredient extends IntersectionChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final MapCodec<IntersectionInfusionIngredient> CODEC = codec(IngredientCreatorAccess.infusion(), IntersectionInfusionIngredient::new);

    IntersectionInfusionIngredient(List<IInfusionIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<IntersectionInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.INTERSECTION.value();
    }
}
