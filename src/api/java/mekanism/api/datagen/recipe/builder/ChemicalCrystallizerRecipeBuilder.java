package mekanism.api.datagen.recipe.builder;

import mekanism.api.datagen.recipe.MekanismRecipeBuilder;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.basic.BasicChemicalCrystallizerRecipe;
import mekanism.api.recipes.book.CrystallizingBookCategory;
import mekanism.api.recipes.book.CrystallizingBookCategory.CrystallizingBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class ChemicalCrystallizerRecipeBuilder extends MekanismRecipeBuilder<CrystallizingBookCategory, CrystallizingBookInfo, ChemicalCrystallizerRecipeBuilder> {

    private final ChemicalStackIngredient input;
    private final ItemStackTemplate output;
    private final SlotDisplay typeDisplay;

    protected ChemicalCrystallizerRecipeBuilder(ChemicalStackIngredient input, SlotDisplay typeDisplay, ItemStackTemplate output) {
        this.input = input;
        this.output = output;
        this.typeDisplay = typeDisplay;
        super(CrystallizingBookInfo::new, CrystallizingBookCategory.MISC);
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(output);
    }

    /// Creates a Chemical Crystallizing recipe builder.
    ///
    /// @param input  Input.
    /// @param output Output.
    public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient input, ItemStackTemplate output) {
        return crystallizing(input, SlotDisplay.Empty.INSTANCE, output);
    }

    /// Creates a Chemical Crystallizing recipe builder.
    ///
    /// @param input       Input.
    /// @param typeDisplay Extra items that should be displayed in the UI to convey the type of crystallization
    /// @param output      Output.
    ///
    /// @since 10.8.0

    public static ChemicalCrystallizerRecipeBuilder crystallizing(ChemicalStackIngredient input, SlotDisplay typeDisplay, ItemStackTemplate output) {
        return new ChemicalCrystallizerRecipeBuilder(input, typeDisplay, output);
    }

    @Override
    protected ChemicalCrystallizerRecipe asRecipe() {
        return new BasicChemicalCrystallizerRecipe(commonInfo(), bookInfo(), input, typeDisplay, output);
    }
}