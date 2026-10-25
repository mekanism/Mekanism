package mekanism.client.gui.recipe_book;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.api.recipes.display.recipe.MekanismRecipeDisplay;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.client.recipebook.RecipeOverlayButtonCreator;

public class MekanismRecipeBookComponent<MENU extends RecipeBookMenu> extends VirtualRecipeBookComponent<MENU> {

    private final List<IGhostRecipeComponent> ghostInputs = new ArrayList<>();
    private final List<IGhostRecipeComponent> ghostOutputs = new ArrayList<>();
    private final MekanismRecipeBookTypes bookCategories;
    private final WidgetSprites filterButtonTextures;

    //TODO - 26.3: When transferring and requiring stacked inputs it doesn't transfer enough.
    // It also then doesn't display ghost things for stuff that can't be transferred for stuff like fluids
    public MekanismRecipeBookComponent(MENU menu, IRecipeLookupHandler<?> recipeLookup, HolderGetter.Provider holderLookup, Supplier<Font> fontSupplier) {
        MekanismRecipeBookTypes bookCategories = recipeLookup.getBookCategories();
        this.bookCategories = bookCategories;
        //TODO - 26.3: Implement
        this.filterButtonTextures = new WidgetSprites(
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
        );
        //Note: Cache index doesn't matter here as the point is to just figure out how many inputs there are
        RecipeContainers recipeContainers = recipeLookup.getRecipeContainers(0);
        RecipeOverlayButtonCreator buttonCreator = (parent, x, y, id, recipe, context, isCraftable) ->
              MekanismOverlayRecipeButton.construct(parent, x, y, id, recipe, context, isCraftable, fontSupplier, recipeContainers.inputs());
        super(menu, fontSupplier, Stream.concat(
              //Append the category as the first tab in the same way TabInfo(SearchRecipeBookCategory category) would construct it
              Stream.of(new RecipeBookComponent.TabInfo(new ItemStack(Items.COMPASS), Optional.empty(), bookCategories)),
              bookCategories.categories().stream().map(category -> new RecipeBookComponent.TabInfo(
                    convertIconToPlaceholder(category.getPrimaryIcon(holderLookup)),
                    category.getSecondaryIcon(holderLookup).map(MekanismRecipeBookComponent::convertIconToPlaceholder),
                    category.toRecipeBookCategory()
              ))
        ).toList(), buttonCreator);
    }

    private static ItemStack convertIconToPlaceholder(Object object) {
        return object instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    public void addComponent(IGhostRecipeComponent component, boolean result) {
        if (result) {
            this.ghostOutputs.add(component);
        } else {
            this.ghostInputs.add(component);
        }
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return filterButtonTextures;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
        selectMatchingRecipes(collection, stackedContents, bookCategories.displayMatches());
    }

    protected final void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents, Predicate<RecipeDisplay> selector) {
        //Instead of calling RecipeCollection#selectRecipes, perform the logic manually so that we can properly have
        // craftable vs not apply when there are only fluids/chemicals required
        for (RecipeDisplayEntry entry : collection.getRecipes()) {
            boolean isSelected = selector.test(entry.display());
            if (isSelected) {
                collection.selected.add(entry.id());
            } else {
                collection.selected.remove(entry.id());
            }
            if (isSelected && canCraft(entry, stackedContents)) {
                collection.craftable.add(entry.id());
            } else {
                collection.craftable.remove(entry.id());
            }
        }
    }

    private boolean canCraft(RecipeDisplayEntry entry, StackedItemContents stackedContents) {
        if (entry.craftingRequirements().isPresent()) {
            List<Ingredient> craftingRequirements = entry.craftingRequirements().get();
            //If we have no ingredients that means we are all chemicals or fluids, so mark it as not being craftable
            // as we don't support transferring chemicals or fluids currently
            //TODO: Can we add support for them by checking capabilities or stuff?
            return !craftingRequirements.isEmpty() && stackedContents.canCraft(craftingRequirements, null);
        }
        return false;
    }

    @Override
    protected Component getRecipeFilterName() {
        return bookCategories.filterName();
    }

    @Override
    protected void clearGhostValues() {
        super.clearGhostValues();
        for (IGhostRecipeComponent ghostInput : ghostInputs) {
            ghostInput.clearGhostData();
        }
        for (IGhostRecipeComponent ghostOutput : ghostOutputs) {
            ghostOutput.clearGhostData();
        }
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipe, ContextMap context) {
        List<SlotDisplay> inputIngredients;
        List<SlotDisplay> outputDisplays;
        if (recipe instanceof FurnaceRecipeDisplay furnaceRecipe) {
            inputIngredients = List.of(furnaceRecipe.ingredient());
            outputDisplays = List.of(furnaceRecipe.result());
        } else if (recipe instanceof MekanismRecipeDisplay recipeDisplay) {
            inputIngredients = recipeDisplay.ingredients();
            outputDisplays = recipeDisplay.rawOutputs();
        } else {
            return;
        }
        SlotSelectTime slotSelectTime = this.recipeBookPage.overlay.slotSelectTime;
        Iterable<SlotDisplay> recipeContext = Iterables.concat(inputIngredients, outputDisplays);
        //TODO - 26.3: Figure out how to handle this for factories/the cache index and how the extra container is at the end of all the primary ones
        int inputCount = ghostInputs.size();
        if (inputCount == inputIngredients.size()) {
            for (int i = 0; i < inputCount; i++) {
                ghostInputs.get(i).setGhostData(inputIngredients.get(i), recipeContext, context, slotSelectTime, false);
            }
        }
        int outputCount = ghostOutputs.size();
        for (int i = 0; i < outputCount; i++) {
            SlotDisplay output;
            if (outputCount == outputDisplays.size()) {
                output = outputDisplays.get(i);
            } else {
                //Note: We allow the resolveForStacks to handle figuring out what elements are valid for items vs a different type of container
                output = recipe.result();
            }
            ghostOutputs.get(i).setGhostData(output, recipeContext, context, slotSelectTime, true);
        }
    }

    @Override
    public void init(Minecraft minecraft) {
        super.init(minecraft);
        this.ghostInputs.clear();
        this.ghostOutputs.clear();
    }
}