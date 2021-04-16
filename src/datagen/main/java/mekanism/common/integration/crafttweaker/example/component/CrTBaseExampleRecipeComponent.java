package mekanism.common.integration.crafttweaker.example.component;

import java.util.Objects;
import mekanism.common.integration.crafttweaker.recipe.MekanismRecipeManager;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.util.ResourceLocation;

public abstract class CrTBaseExampleRecipeComponent implements ICrTExampleComponent {

    protected final ResourceLocation recipeType;

    public CrTBaseExampleRecipeComponent(MekanismRecipeManager<?> recipeManager) {
        Objects.requireNonNull(recipeManager, "Recipe manager cannot be null.");
        this.recipeType = ((MekanismRecipeType<?, ?>) recipeManager.getRecipeType()).getRegistryName();
    }

    protected void appendRecipeMethodStart(StringBuilder stringBuilder, String methodName) {
        stringBuilder.append("<recipetype:")
              .append(recipeType)
              .append(">.")
              .append(methodName)
              .append('(');
    }
}