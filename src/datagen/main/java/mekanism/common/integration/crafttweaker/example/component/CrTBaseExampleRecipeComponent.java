package mekanism.common.integration.crafttweaker.example.component;

import java.util.Objects;
import mekanism.common.integration.crafttweaker.recipe.manager.MekanismRecipeManager;
import net.minecraft.resources.ResourceLocation;

public abstract class CrTBaseExampleRecipeComponent implements ICrTExampleComponent {

    protected final ResourceLocation recipeType;

    public CrTBaseExampleRecipeComponent(MekanismRecipeManager<?, ?> recipeManager) {
        Objects.requireNonNull(recipeManager, "Recipe manager cannot be null.");
        this.recipeType = recipeManager.getBracketResourceLocation();
    }

    protected void appendRecipeMethodStart(StringBuilder stringBuilder, String methodName) {
        stringBuilder.append("<recipetype:")
              .append(recipeType)
              .append(">.")
              .append(methodName)
              .append('(');
    }
}