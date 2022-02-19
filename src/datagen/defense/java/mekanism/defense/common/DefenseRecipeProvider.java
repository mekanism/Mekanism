package mekanism.defense.common;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.common.recipe.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.common.data.ExistingFileHelper;

@ParametersAreNonnullByDefault
public class DefenseRecipeProvider extends BaseRecipeProvider {

    public DefenseRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, MekanismDefense.MODID);
    }

    @Override
    protected void addRecipes(Consumer<IFinishedRecipe> consumer) {
    }
}