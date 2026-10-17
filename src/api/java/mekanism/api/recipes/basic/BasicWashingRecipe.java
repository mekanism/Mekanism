package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.api.recipes.book.WashingBookCategory.WashingBookInfo;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicWashingRecipe extends BasicFluidChemicalToChemicalRecipe<WashingBookCategory, WashingBookInfo> {

    private static final Holder<Item> CHEMICAL_WASHER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_washer"));

    /// @param commonInfo    Common recipe info.
    /// @param bookInfo      Recipe book info.
    /// @param fluidInput    Fluid input.
    /// @param chemicalInput Chemical input.
    /// @param output        Output.
    public BasicWashingRecipe(CommonInfo commonInfo, WashingBookInfo bookInfo, FluidStackIngredient fluidInput, ChemicalStackIngredient chemicalInput,
          ChemicalStackTemplate output) {
        super(MekanismRecipeTypes.TYPE_WASHING.value(), commonInfo, bookInfo, fluidInput, chemicalInput, output);
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CombiningRecipeDisplay(
              getChemicalInput().display(),
              getFluidInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_WASHER)
        ));
    }

    @Override
    public RecipeSerializer<BasicWashingRecipe> getSerializer() {
        return MekanismRecipeSerializers.WASHING.get();
    }
}