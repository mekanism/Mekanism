package mekanism.api.datagen.recipe.builder;

import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.basic.BasicChemicalDissolutionRecipe;
import mekanism.api.recipes.book.DissolutionBookCategory;
import mekanism.api.recipes.book.DissolutionBookCategory.DissolutionBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class ChemicalDissolutionRecipeBuilder extends MekanismRecipeBuilder<DissolutionBookCategory, DissolutionBookInfo, ChemicalDissolutionRecipeBuilder> {

    private final ItemStackIngredient itemInput;
    private final ChemicalStackIngredient chemicalInput;
    private final ChemicalStackTemplate output;
    private final boolean perTickUsage;

    protected ChemicalDissolutionRecipeBuilder(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage, ChemicalStackTemplate output) {
        this.itemInput = itemInput;
        this.chemicalInput = chemicalInput;
        this.perTickUsage = perTickUsage;
        this.output = output;
        super(DissolutionBookInfo::new, DissolutionBookCategory.MISC);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return getDefaultRecipeId(output);
    }

    /// Creates a Chemical Dissolution recipe builder.
    ///
    /// @param itemInput     Item Input.
    /// @param chemicalInput Chemical Input.
    /// @param perTickUsage  Should the recipe consume the chemical input each tick it is processing.
    /// @param output        Output.
    public static ChemicalDissolutionRecipeBuilder dissolution(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, boolean perTickUsage, ChemicalStackTemplate output) {
        return new ChemicalDissolutionRecipeBuilder(itemInput, chemicalInput, perTickUsage, output);
    }

    @Override
    protected ChemicalDissolutionRecipe asRecipe() {
        return new BasicChemicalDissolutionRecipe(commonInfo(), bookInfo(), itemInput, chemicalInput, perTickUsage, output);
    }
}