package mekanism.api.datagen.recipe.builder;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.basic.BasicElectrolysisRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

@NothingNullByDefault
public class ElectrolysisRecipeBuilder extends MekanismRecipeBuilder<ElectrolysisRecipeBuilder> {

    private final FluidStackIngredient input;
    private final ChemicalStack leftChemicalOutput;
    private final ChemicalStack rightChemicalOutput;
    private int energyMultiplier = 1;

    protected ElectrolysisRecipeBuilder(FluidStackIngredient input, ChemicalStack leftChemicalOutput, ChemicalStack rightChemicalOutput) {
        this.input = input;
        this.leftChemicalOutput = leftChemicalOutput;
        this.rightChemicalOutput = rightChemicalOutput;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        Identifier leftId = chemicalId(leftChemicalOutput);
        Identifier rightId = chemicalId(rightChemicalOutput);
        Identifier combinedId = Identifier.fromNamespaceAndPath(leftId.getNamespace(), leftId.getPath() + "_" + rightId.getPath());
        return ResourceKey.create(Registries.RECIPE, combinedId);
    }

    /**
     * Creates a Separating recipe builder.
     *
     * @param input               Input.
     * @param leftChemicalOutput  Left Output.
     * @param rightChemicalOutput Right Output.
     */
    public static ElectrolysisRecipeBuilder separating(FluidStackIngredient input, ChemicalStack leftChemicalOutput, ChemicalStack rightChemicalOutput) {
        if (leftChemicalOutput.isEmpty() || rightChemicalOutput.isEmpty()) {
            throw new IllegalArgumentException("This separating recipe requires non empty chemical outputs.");
        }
        return new ElectrolysisRecipeBuilder(input, leftChemicalOutput, rightChemicalOutput);
    }

    /**
     * Sets the energy multiplier for this recipe.
     *
     * @param multiplier Multiplier to the energy cost in relation to the configured hydrogen separating energy cost. This value must be greater than or equal to one.
     */
    public ElectrolysisRecipeBuilder energyMultiplier(int multiplier) {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Energy multiplier must be greater than or equal to one");
        }
        this.energyMultiplier = multiplier;
        return this;
    }

    @Override
    protected ElectrolysisRecipe asRecipe() {
        return new BasicElectrolysisRecipe(input, energyMultiplier, leftChemicalOutput, rightChemicalOutput);
    }
}