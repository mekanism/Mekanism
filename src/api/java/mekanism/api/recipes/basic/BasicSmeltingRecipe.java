package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
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

public class BasicSmeltingRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private static final Holder<Item> ENERGIZED_SMELTER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "energized_smelter"));

    public BasicSmeltingRecipe(ItemStackIngredient input, ItemStackTemplate output) {
        super(input, output, MekanismRecipeTypes.TYPE_SMELTING.value());
    }

    @Override
    public RecipeSerializer<BasicSmeltingRecipe> getSerializer() {
        return MekanismRecipeSerializers.SMELTING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(ENERGIZED_SMELTER)
        ));
    }
}