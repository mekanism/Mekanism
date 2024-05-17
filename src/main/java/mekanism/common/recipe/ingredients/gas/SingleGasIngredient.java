package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismGasIngredientTypes;
import net.minecraft.core.Holder;

@NothingNullByDefault
public final class SingleGasIngredient extends SingleChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<SingleGasIngredient> CODEC = GasStack.GAS_NON_EMPTY_HOLDER_CODEC.xmap(SingleGasIngredient::new, SingleGasIngredient::chemical)
          .fieldOf(JsonConstants.GAS);

    SingleGasIngredient(Holder<Gas> gas) {
        super(gas);
    }

    @Override
    public MapCodec<SingleGasIngredient> codec() {
        return MekanismGasIngredientTypes.SINGLE.value();
    }
}
