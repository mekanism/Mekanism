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

public class BasicChemicalOxidizerRecipe extends BasicItemStackToChemicalRecipe {

    private static final Holder<Item> CHEMICAL_OXIDIZER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_oxidizer"));

    public BasicChemicalOxidizerRecipe(ItemStackIngredient input, ChemicalStackTemplate output) {
        super(input, output, MekanismRecipeTypes.TYPE_OXIDIZING.value());
    }

    @Override
    public RecipeSerializer<BasicChemicalOxidizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_OXIDIZER)
        ));
    }
}