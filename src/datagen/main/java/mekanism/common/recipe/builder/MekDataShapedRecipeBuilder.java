package mekanism.common.recipe.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MekDataShapedRecipeBuilder extends ExtendedShapedRecipeBuilder {

    private MekDataShapedRecipeBuilder(IItemProvider result, int count) {
        super(MekanismRecipeSerializers.MEK_DATA.getRecipeSerializer(), result, count);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(IItemProvider result) {
        return shapedRecipe(result, 1);
    }

    public static MekDataShapedRecipeBuilder shapedRecipe(IItemProvider result, int count) {
        return new MekDataShapedRecipeBuilder(result, count);
    }
}