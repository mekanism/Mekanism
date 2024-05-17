package mekanism.common.recipe.ingredients.slurry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.chemical.IntersectionChemicalIngredient;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.chemical.ISlurryIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.registries.MekanismSlurryIngredientTypes;

@NothingNullByDefault
public final class IntersectionSlurryIngredient extends IntersectionChemicalIngredient<Slurry, ISlurryIngredient> implements ISlurryIngredient {

    public static final MapCodec<IntersectionSlurryIngredient> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
          IngredientCreatorAccess.slurry().listCodecMultipleElements().fieldOf(JsonConstants.CHILDREN).forGetter(IntersectionSlurryIngredient::children)
    ).apply(builder, IntersectionSlurryIngredient::new));

    IntersectionSlurryIngredient(List<ISlurryIngredient> children) {
        super(children);
    }

    @Override
    public MapCodec<IntersectionSlurryIngredient> codec() {
        return MekanismSlurryIngredientTypes.INTERSECTION.value();
    }
}
