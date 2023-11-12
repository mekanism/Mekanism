package mekanism.common.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public class MekDataShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private MekDataShapedRecipeBuilder(ItemLike result, int count) {
        super(MekanismRecipeSerializersInternal.MEK_DATA.get(), result, count);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(ItemLike result) {
        return shapedRecipe(result, 1);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(ItemLike result, int count) {
        return new MekDataShapedRecipeBuilder(result, count);
    }
}