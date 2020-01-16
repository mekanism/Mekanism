package mekanism.common.recipe;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

//TODO: Make this extend a class that is provided in the API package instead, with helpers for registering Mekanism machine recipes
// Or maybe have it be more like the CustomRecipeBuilder and ShapedRecipeBuilder classes
//TODO: Evaluate the bio fuel recipes, and maybe add sweet berry bush/coral to it (and maybe bamboo sapling, and honey). Also add missing flowers that don't get
// caught by the small flowers tag
public abstract class BaseRecipeGenerator extends RecipeProvider {

    public BaseRecipeGenerator(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected abstract void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer);
}