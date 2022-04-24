package mekanism.tools.common.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.ItemLike;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PaxelShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private PaxelShapedRecipeBuilder(ItemLike result, int count) {
        super(ToolsRecipeSerializers.PAXEL.get(), result, count);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(ItemLike result) {
        return shapedRecipe(result, 1);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(ItemLike result, int count) {
        return new PaxelShapedRecipeBuilder(result, count);
    }
}