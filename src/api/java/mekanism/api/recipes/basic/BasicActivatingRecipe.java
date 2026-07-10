package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicActivatingRecipe extends BasicChemicalToChemicalRecipe {

    private static final Holder<Item> SOLAR_NEUTRON_ACTIVATOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "solar_neutron_activator"));

    public BasicActivatingRecipe(ChemicalStackIngredient input, ChemicalStackTemplate output) {
        super(input, output, MekanismRecipeTypes.TYPE_ACTIVATING.value());
    }

    @Override
    public RecipeSerializer<BasicActivatingRecipe> getSerializer() {
        return MekanismRecipeSerializers.ACTIVATING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(SOLAR_NEUTRON_ACTIVATOR)
        ));
    }
}