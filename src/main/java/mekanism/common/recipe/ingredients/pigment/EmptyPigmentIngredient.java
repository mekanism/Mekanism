package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class EmptyPigmentIngredient extends EmptyChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final EmptyPigmentIngredient INSTANCE = new EmptyPigmentIngredient();

    public static final MapCodec<EmptyPigmentIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyPigmentIngredient() {
    }

    @Override
    public MapCodec<EmptyPigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.EMPTY.value();
    }
}
