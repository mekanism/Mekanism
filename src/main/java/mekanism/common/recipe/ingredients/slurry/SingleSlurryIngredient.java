package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismSlurryIngredientTypes;
import net.minecraft.core.Holder;

@NothingNullByDefault
public final class SingleSlurryIngredient extends SingleChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<SingleSlurryIngredient> CODEC = SlurryStack.SLURRY_NON_EMPTY_HOLDER_CODEC.xmap(SingleSlurryIngredient::new, SingleSlurryIngredient::chemical)
          .fieldOf(SerializationConstants.SLURRY);

    SingleSlurryIngredient(Holder<Slurry> slurry) {
        super(slurry);
    }

    @Override
    public MapCodec<SingleSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.SINGLE.value();
    }
}
