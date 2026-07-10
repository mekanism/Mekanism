package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicChemicalConversionRecipe extends BasicItemStackToChemicalRecipe {

    private static final Holder<Item> CREATIVE_CHEMICAL_TANK = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "creative_chemical_tank"));

    public BasicChemicalConversionRecipe(ItemStackIngredient input, ChemicalStackTemplate output) {
        super(input, output, MekanismRecipeTypes.TYPE_CHEMICAL_CONVERSION.value());
    }

    @Override
    public RecipeSerializer<BasicChemicalConversionRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_CONVERSION.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              //TODO - 26.2: What do we want to display as the work stations here
              new SlotDisplay.ItemSlotDisplay(CREATIVE_CHEMICAL_TANK)
        ));
    }
}