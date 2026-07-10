package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.MixingRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicChemicalInfuserRecipe extends BasicChemicalChemicalToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_INFUSER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_infuser"));

    /// @param leftInput  Left input.
    /// @param rightInput Right input.
    /// @param output     Output.
    ///
    /// @apiNote The order of the inputs does not matter.
    public BasicChemicalInfuserRecipe(ChemicalStackIngredient leftInput, ChemicalStackIngredient rightInput, ChemicalStackTemplate output) {
        super(leftInput, rightInput, output, MekanismRecipeTypes.TYPE_CHEMICAL_INFUSING.value());
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new MixingRecipeDisplay(
              getLeftInput().display(),
              getRightInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_INFUSER)
        ));
    }

    @Override
    public RecipeSerializer<BasicChemicalInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_INFUSING.get();
    }
}