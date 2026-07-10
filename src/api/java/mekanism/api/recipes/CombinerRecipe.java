package mekanism.api.recipes;

import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.TwoInputMekRecipe.SimpleTwoInputRecipe;
import mekanism.api.recipes.display.CombiningRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Main Input: ItemStack
///
/// Secondary/Extra Input: ItemStack
///
/// Output: ItemStack
///
/// @apiNote Combiners and Combining Factories can process this recipe type.
public abstract class CombinerRecipe extends SimpleTwoInputRecipe<Item, ItemStack, ItemStackIngredient, RecipeInput, ItemStackTemplate> {

    private static final Holder<Item> COMBINER = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "combiner"));

    /// Gets the main input ingredient.
    public abstract ItemStackIngredient getMainInput();

    @Override
    public final ItemStackIngredient getInputA() {
        return getMainInput();
    }

    /// Gets the secondary input ingredient.
    public abstract ItemStackIngredient getExtraInput();

    @Override
    public final ItemStackIngredient getInputB() {
        return getExtraInput();
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        if (!isIncomplete() && input.size() == 2) {
            ItemStack mainInput = input.getItem(0);
            ItemStack extraInput = input.getItem(1);
            if (test(mainInput, extraInput)) {
                return getOutput(mainInput, extraInput).create();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        //Don't match incomplete recipes or ones that don't match
        return !isIncomplete() && input.size() == 2 && test(input.getItem(0), input.getItem(1));
    }

    @Override
    public final RecipeType<CombinerRecipe> getType() {
        return MekanismRecipeTypes.TYPE_COMBINING.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new CombiningRecipeDisplay(
              getMainInput().display(),
              getExtraInput().display(),
              getOutputDisplay(),
              new SlotDisplay.ItemSlotDisplay(COMBINER)
        ));
    }
}
