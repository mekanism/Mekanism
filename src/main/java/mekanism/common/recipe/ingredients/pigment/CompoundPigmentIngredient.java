package mekanism.common.recipe.ingredients.pigment;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.recipes.ingredients.chemical.CompoundChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.IPigmentIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismPigmentIngredientTypes;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

@NothingNullByDefault
public final class CompoundPigmentIngredient extends CompoundChemicalIngredient<Pigment, IPigmentIngredient> implements IPigmentIngredient {

    public static final MapCodec<CompoundPigmentIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(IngredientCreatorAccess.basicPigment().listCodecMultipleElements(),
          JsonConstants.CHILDREN, JsonConstants.INGREDIENTS).xmap(
          CompoundPigmentIngredient::new, CompoundPigmentIngredient::children
    );

    CompoundPigmentIngredient(List<IPigmentIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<CompoundPigmentIngredient> codec() {
        return MekanismPigmentIngredientTypes.COMPOUND.value();
    }
}
