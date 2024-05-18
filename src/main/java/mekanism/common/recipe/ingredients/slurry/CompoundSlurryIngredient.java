package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class CompoundSlurryIngredient extends CompoundChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<CompoundSlurryIngredient> CODEC = codec(IngredientCreatorAccess.slurry(), CompoundSlurryIngredient::new);

    CompoundSlurryIngredient(List<ISlurryIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.COMPOUND.value();
    }
}
