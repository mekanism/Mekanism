package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.EvaporatingBookCategory;
import mekanism.api.recipes.book.EvaporatingBookCategory.EvaporatingBookInfo;
import mekanism.api.recipes.display.recipe.EvaporatingRecipeDisplay;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicEvaporatingRecipe extends BasicFluidToFluidRecipe<EvaporatingBookCategory, EvaporatingBookInfo> {

    private static final Holder<Item> THERMAL_EVAPORATION_CONTROLLER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "thermal_evaporation_controller"));

    /// @param commonInfo Common recipe info.
    /// @param bookInfo   Recipe book info.
    /// @param input      Input.
    /// @param output     Output.
    ///
    /// @since 10.8.0
    public BasicEvaporatingRecipe(CommonInfo commonInfo, EvaporatingBookInfo bookInfo, FluidStackIngredient input, FluidStackTemplate output) {
        super(MekanismRecipeTypes.TYPE_EVAPORATING.value(), commonInfo, bookInfo, input, output);
    }

    @Override
    public RecipeSerializer<BasicEvaporatingRecipe> getSerializer() {
        return MekanismRecipeSerializers.EVAPORATING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new EvaporatingRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(THERMAL_EVAPORATION_CONTROLLER)
        ));
    }
}