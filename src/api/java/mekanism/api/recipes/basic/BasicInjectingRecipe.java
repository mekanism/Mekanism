package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory.InjectingBookInfo;
import mekanism.api.recipes.display.recipe.InjectingRecipeDisplay;
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

public class BasicInjectingRecipe extends BasicItemStackChemicalToItemStackRecipe<InjectingBookCategory, InjectingBookInfo> {

    private static final Holder<Item> CHEMICAL_INJECTION_CHAMBER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_injection_chamber"));

    public BasicInjectingRecipe(CommonInfo commonInfo, InjectingBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          boolean perTickUsage, ItemStackTemplate output) {
        super(MekanismRecipeTypes.TYPE_INJECTING.value(), commonInfo, bookInfo, itemInput, chemicalInput, perTickUsage, output);
    }

    @Override
    public RecipeSerializer<BasicInjectingRecipe> getSerializer() {
        return MekanismRecipeSerializers.INJECTING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new InjectingRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(CHEMICAL_INJECTION_CHAMBER)
        ));
    }
}