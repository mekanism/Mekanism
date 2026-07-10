package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.SingleInputRecipe.FluidInputRecipe;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Input: FluidStack
///
/// Output: FluidStack
///
/// @apiNote Thermal Evaporation Towers can process this recipe type.
public abstract class FluidToFluidRecipe extends FluidInputRecipe<FluidStackTemplate> {

    private static final Holder<Item> THERMAL_EVAPORATION_CONTROLLER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "thermal_evaporation_controller"));

    @Override
    public final RecipeType<FluidToFluidRecipe> getType() {
        return MekanismRecipeTypes.TYPE_EVAPORATING.get();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(THERMAL_EVAPORATION_CONTROLLER)
        ));
    }
}
