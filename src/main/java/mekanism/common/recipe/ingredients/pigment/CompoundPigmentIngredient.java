package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;

@NothingNullByDefault
public final class CompoundPigmentIngredient extends CompoundChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<CompoundPigmentIngredient> CODEC = codec(IngredientCreatorAccess.pigment(), CompoundPigmentIngredient::new);

    CompoundPigmentIngredient(List<IPigmentIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundPigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.COMPOUND.value();
    }
}
