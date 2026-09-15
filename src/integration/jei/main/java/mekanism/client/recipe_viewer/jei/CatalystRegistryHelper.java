package mekanism.client.recipe_viewer.jei;

import java.util.List;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class CatalystRegistryHelper {

    private CatalystRegistryHelper() {
    }

    public static void register(IRecipeCatalystRegistration registry, IRecipeViewerRecipeType<?>... categories) {
        for (IRecipeViewerRecipeType<?> category : categories) {
            register(registry, MekanismJEI.genericRecipeType(category), category.workstations());
        }
    }

    public static void register(IRecipeCatalystRegistration registry, IRecipeType<?> recipeType, List<SlotDisplay> workstations) {
        for (SlotDisplay workstation : workstations) {
            registry.addCraftingStation(recipeType, workstation);
        }
    }
}