package mekanism.defense.common;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;

@ParametersAreNonnullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(DataGenerator gen) {
        super(gen, MekanismDefense.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        super.registerRecipes(consumer);
    }
}