package mekanism.common.recipe.ingredients.gas;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IGasIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismGasIngredientTypes;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

@NothingNullByDefault
public final class CompoundGasIngredient extends CompoundChemicalIngredient<Gas, IGasIngredient> implements IGasIngredient {

    public static final MapCodec<CompoundGasIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(IngredientCreatorAccess.gas().listCodecMultipleElements(),
          JsonConstants.CHILDREN, JsonConstants.INGREDIENTS).xmap(
          CompoundGasIngredient::new, CompoundGasIngredient::children
    );

    CompoundGasIngredient(List<IGasIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundGasIngredient> codec() {
        return MekanismGasIngredientTypes.COMPOUND.value();
    }
}
