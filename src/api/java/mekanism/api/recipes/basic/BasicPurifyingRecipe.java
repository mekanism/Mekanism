package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.RateBasedCombiningRecipeDisplay;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicPurifyingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> PURIFICATION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "purification_chamber"));

    public BasicPurifyingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MekanismRecipeTypes.TYPE_PURIFYING.value());
    }

    @Override
    public RecipeSerializer<BasicPurifyingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PURIFYING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new RateBasedCombiningRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(PURIFICATION_CHAMBER)
        ));
    }
}