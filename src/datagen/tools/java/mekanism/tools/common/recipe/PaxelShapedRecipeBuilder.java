package mekanism.tools.common.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PaxelShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private PaxelShapedRecipeBuilder(IItemProvider result, int count) {
        super(ToolsRecipeSerializers.PAXEL.getRecipeSerializer(), result, count);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(IItemProvider result) {
        return shapedRecipe(result, 1);
    }

    public static PaxelShapedRecipeBuilder shapedRecipe(IItemProvider result, int count) {
        return new PaxelShapedRecipeBuilder(result, count);
    }
}