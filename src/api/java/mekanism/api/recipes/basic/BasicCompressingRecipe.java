package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.display.PerTickCombiningRecipeDisplay;
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

public class BasicCompressingRecipe extends BasicItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo> {

    private static final Holder<Item> OSMIUM_COMPRESSOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "osmium_compressor"));

    public BasicCompressingRecipe(CommonInfo commonInfo, CompressingBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          boolean perTickUsage, ItemStackTemplate output) {
        super(MekanismRecipeTypes.TYPE_COMPRESSING.value(), commonInfo, bookInfo, itemInput, chemicalInput, perTickUsage, output);
    }

    @Override
    public RecipeSerializer<BasicCompressingRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(PerTickCombiningRecipeDisplay.create(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(OSMIUM_COMPRESSOR)
        ));
    }
}