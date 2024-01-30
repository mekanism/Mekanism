package mekanism.defense.common;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper);
    }

    @Override
    protected void addRecipes(RecipeOutput consumer) {
    }
}