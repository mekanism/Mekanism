package mekanism.common.integration.crafttweaker.example.component;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.integration.crafttweaker.example.BaseCrTExampleProvider;
import mekanism.common.integration.crafttweaker.recipe.manager.MekanismRecipeManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class CrTExampleRemoveRecipesComponent extends CrTBaseExampleRecipeComponent {

    private final List<ResourceLocation> recipesToRemove = new ArrayList<>();

    public CrTExampleRemoveRecipesComponent(BaseCrTExampleProvider exampleProvider, MekanismRecipeManager<?, ?> recipeManager, ResourceLocation... recipeNames) {
        super(recipeManager);
        if (recipeNames == null || recipeNames.length == 0) {
            throw new IllegalArgumentException("No recipes to remove specified.");
        }
        for (ResourceLocation recipeName : recipeNames) {
            if (exampleProvider.recipeExists(recipeName)) {
                if (recipesToRemove.contains(recipeName)) {
                    //Note: This isn't the most accurate already removing example check as it doesn't check other removing components
                    // with the same recipe manager, but it should be fine as it is mainly to help try and prevent copy-paste errors
                    throw new IllegalArgumentException("Example removal of recipe '" + recipeName + "' declared multiple times.");
                }
                //TODO: Eventually it would be nice if this could validate the recipe type, but it isn't really all that important currently
                recipesToRemove.add(recipeName);
            } else {
                throw new IllegalArgumentException("Recipe '" + recipeName + "' does not exist.");
            }
        }
    }

    @NotNull
    @Override
    public String asString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("// ");
        appendRecipeMethodStart(stringBuilder, "removeByName");
        stringBuilder.append("name as string)\n\n");
        for (int i = 0, recipesToRemoveSize = recipesToRemove.size(); i < recipesToRemoveSize; i++) {
            appendRecipeMethodStart(stringBuilder, "removeByName");
            stringBuilder.append('"')
                  .append(recipesToRemove.get(i))
                  .append("\");");
            if (i < recipesToRemoveSize - 1) {
                //If we are not on the last line add a new line after this
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }
}