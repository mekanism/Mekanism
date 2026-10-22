package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory.MetallurgicInfusingBookInfo;
import mekanism.api.recipes.display.recipe.MetallurgicInfusingRecipeDisplay;
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

public class BasicMetallurgicInfuserRecipe extends BasicItemStackChemicalToItemStackRecipe<MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo> {

    private static final Holder<Item> METALLURGIC_INFUSER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "metallurgic_infuser"));

    /// @param itemInput     Item input.
    /// @param chemicalInput Infusion input.
    /// @param output        Output.
    public BasicMetallurgicInfuserRecipe(CommonInfo commonInfo, MetallurgicInfusingBookInfo bookInfo, ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput,
          boolean perTickUsage, ItemStackTemplate output) {
        super(MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING.value(), commonInfo, bookInfo, itemInput, chemicalInput, perTickUsage, output);
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new MetallurgicInfusingRecipeDisplay(
              getItemInput().display(),
              getChemicalInput().display(),
              perTickUsage(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(METALLURGIC_INFUSER)
        ));
    }

    @Override
    public RecipeSerializer<BasicMetallurgicInfuserRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSING.get();
    }
}