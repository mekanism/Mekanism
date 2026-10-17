package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.ElectrolysisRecipe.ElectrolysisRecipeOutput;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.book.ElectrolysisBookCategory;
import mekanism.api.recipes.book.ElectrolysisBookCategory.ElectrolysisBookInfo;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisBookCategory, ElectrolysisBookInfo, ElectrolysisRecipeBuilder> {

    private final FluidStackIngredient input;
    private final ElectrolysisRecipeOutput output;
    private int energyMultiplier = 1;

    protected ElectrolysisRecipeBuilder(FluidStackIngredient input, ElectrolysisRecipeOutput output) {
        this.input = input;
        this.output = output;
        super(ElectrolysisBookInfo::new, ElectrolysisBookCategory.MISC);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        Identifier leftId = instanceId(output.left());
        Identifier rightId = instanceId(output.right());
        return ResourceKey.create(Registries.RECIPE, leftId.withSuffix("_" + rightId.getPath()));
    }

    /// Creates a Separating recipe builder.
    ///
    /// @param input               Input.
    /// @param leftChemicalOutput  Left Output.
    /// @param rightChemicalOutput Right Output.
    public static ElectrolysisRecipeBuilder separating(FluidStackIngredient input, ChemicalStackTemplate leftChemicalOutput, ChemicalStackTemplate rightChemicalOutput) {
        return new ElectrolysisRecipeBuilder(input, new ElectrolysisRecipeOutput(leftChemicalOutput, rightChemicalOutput));
    }

    /// Sets the energy multiplier for this recipe.
    ///
    /// @param multiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. This value must be greater than or equal to one.
    public ElectrolysisRecipeBuilder energyMultiplier(int multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be greater than or equal to one");
        }
        this.energyMultiplier = multiplier;
        return this;
    }

    @Override
    protected ElectrolysisRecipe asRecipe() {
        return new BasicElectrolysisRecipe(commonInfo(), bookInfo(), input, energyMultiplier, output);
    }
}