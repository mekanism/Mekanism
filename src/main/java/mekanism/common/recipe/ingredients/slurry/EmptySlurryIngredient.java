package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class EmptySlurryIngredient extends EmptyChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final EmptySlurryIngredient INSTANCE = new EmptySlurryIngredient();
    public static final MapCodec<EmptySlurryIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptySlurryIngredient() {
    }

    @Override
    public MapCodec<EmptySlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.EMPTY.value();
    }
}
