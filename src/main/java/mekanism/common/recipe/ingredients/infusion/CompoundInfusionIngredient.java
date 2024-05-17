package mekanism.common.recipe.ingredients.infusion;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IInfusionIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismInfusionIngredientTypes;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

@NothingNullByDefault
public final class CompoundInfusionIngredient extends CompoundChemicalIngredient<InfuseType, IInfusionIngredient> implements IInfusionIngredient {

    public static final MapCodec<CompoundInfusionIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(IngredientCreatorAccess.infusion().listCodecMultipleElements(),
          JsonConstants.CHILDREN, JsonConstants.INGREDIENTS).xmap(
          CompoundInfusionIngredient::new, CompoundInfusionIngredient::children
    );

    CompoundInfusionIngredient(List<IInfusionIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundInfusionIngredient> codec() {
        return MekanismInfusionIngredientTypes.COMPOUND.value();
    }
}
