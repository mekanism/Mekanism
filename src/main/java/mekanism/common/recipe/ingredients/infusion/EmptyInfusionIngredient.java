package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.EmptyChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.common.registries.MekanismInfusionIngredientTypes;

@NothingNullByDefault
public final class EmptyInfusionIngredient extends EmptyChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final EmptyInfusionIngredient INSTANCE = new EmptyInfusionIngredient();
    public static final MapCodec<EmptyInfusionIngredient> CODEC = MapCodec.unit(INSTANCE);

    private EmptyInfusionIngredient() {
    }

    @Override
    public MapCodec<EmptyInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.EMPTY.value();
    }
}
