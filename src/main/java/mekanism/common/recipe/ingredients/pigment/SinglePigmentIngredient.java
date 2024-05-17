package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismPigmentIngredientTypes;
import net.minecraft.core.Holder;

@NothingNullByDefault
public final class SinglePigmentIngredient extends SingleChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<SinglePigmentIngredient> CODEC = PigmentStack.PIGMENT_NON_EMPTY_HOLDER_CODEC.xmap(SinglePigmentIngredient::new, SinglePigmentIngredient::chemical)
          .fieldOf(JsonConstants.PIGMENT);

    SinglePigmentIngredient(Holder<Pigment> pigment) {
        super(pigment);
    }

    @Override
    public MapCodec<SinglePigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.SINGLE.value();
    }
}
