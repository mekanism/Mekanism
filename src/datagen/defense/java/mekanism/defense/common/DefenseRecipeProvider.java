package mekanism.defense.common;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismDefense.MODID);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
    }
}