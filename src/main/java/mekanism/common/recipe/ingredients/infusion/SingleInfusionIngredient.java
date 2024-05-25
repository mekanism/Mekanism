package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.common.registries.MekanismInfusionIngredientTypes;
import net.minecraft.core.Holder;

@NothingNullByDefault
public final class SingleInfusionIngredient extends SingleChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final MapCodec<SingleInfusionIngredient> CODEC = InfusionStack.INFUSE_TYPE_NON_EMPTY_HOLDER_CODEC.xmap(SingleInfusionIngredient::new, SingleInfusionIngredient::chemical)
          .fieldOf(SerializationConstants.INFUSE_TYPE);

    SingleInfusionIngredient(Holder<InfuseType> infuseType) {
        super(infuseType);
    }

    @Override
    public MapCodec<SingleInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.SINGLE.value();
    }
}
