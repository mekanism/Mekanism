package mekanism.client.gui.recipe_book;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.api.recipes.display.recipe.MekanismRecipeDisplay;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.client.RecipeBookManager;
import org.jspecify.annotations.Nullable;

//TODO - 26.4: Double check nothing changed that might have broken this, the overlay, or the ghost slots
public class MekanismRecipeBookComponent<MENU extends MekanismContainer> extends RecipeBookComponent<MENU> {

    private final List<IGhostRecipeComponent> ghostInputs = new ArrayList<>();
    private final List<IGhostRecipeComponent> ghostOutputs = new ArrayList<>();
    private final MekanismRecipeBookTypes bookCategories;
    private final WidgetSprites filterButtonTextures;
    private int x;
    private int y;

    //TODO - 26.3: When transferring it then shows as red still? Maybe because it doesn't transfer enough? Seen on PRC
    //TODO - 26.3: SessionSearchTrees#updateRecipes make it support things other than items?
    public MekanismRecipeBookComponent(MENU menu, IRecipeLookupHandler<?> recipeLookup, Supplier<Font> fontSupplier) {
        MekanismRecipeBookTypes bookCategories = recipeLookup.getBookCategories();
        this.bookCategories = bookCategories;
        //TODO - 26.3: Implement
        this.filterButtonTextures = new WidgetSprites(
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_enabled_highlighted"),
              Identifier.withDefaultNamespace("recipe_book/furnace_filter_disabled_highlighted")
        );
        super(menu, Stream.concat(
              //Append the category as the first tab in the same way TabInfo(SearchRecipeBookCategory category) would construct it
              Stream.of(new RecipeBookComponent.TabInfo(new ItemStack(Items.COMPASS), Optional.empty(), bookCategories)),
              bookCategories.categories().stream().map(category -> new RecipeBookComponent.TabInfo(
                    category.getPrimaryIcon(), category.getSecondaryIcon(), category.toRecipeBookCategory()
              ))
        ).toList());
        //Note: Cache index doesn't matter here as the point is to just figure out how many inputs there are
        RecipeContainers recipeContainers = recipeLookup.getRecipeContainers(0);
        this.recipeBookPage = new MekanismRecipeBookPage(this, this.recipeBookPage.overlay.slotSelectTime, fontSupplier, recipeContainers.inputs());
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
    protected boolean isCraftingSlot(Slot slot) {
        return slot instanceof InventoryContainerSlot inventorySlot && inventorySlot.isRecipeCraftingSlot();
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
            return !craftingRequirements.isEmpty() && stackedContents.canCraft(craftingRequirements, null);
        }
        return false;
    }

    @Override
    protected Component getRecipeFilterName() {
        return bookCategories.filterName();
    }

    private void clearGhostValues() {
        for (IGhostRecipeComponent ghostInput : ghostInputs) {
            ghostInput.clearGhostData();
        }
        for (IGhostRecipeComponent ghostOutput : ghostOutputs) {
            ghostOutput.clearGhostData();
        }
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay recipe) {
        clearGhostValues();
        super.fillGhostRecipe(recipe);
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
        //TODO - 26.3: Figure out how to handle this for factories/the cache index
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
    public void slotClicked(@Nullable Slot slot) {
        if (slot != null && this.isCraftingSlot(slot)) {
            clearGhostValues();
            super.slotClicked(slot);
        }
    }

    @Override
    protected boolean tryPlaceRecipe(RecipeCollection recipeCollection, RecipeDisplayId recipe, boolean useMaxItems) {
        if (super.tryPlaceRecipe(recipeCollection, recipe, useMaxItems)) {
            clearGhostValues();
            return true;
        }
        return false;
    }

    public void init(Minecraft minecraft) {
        init(0, 0, minecraft, false);
        this.ghostInputs.clear();
        this.ghostOutputs.clear();
    }

    @Override
    public int getXOrigin() {
        return x;
    }

    @Override
    public int getYOrigin() {
        return y;
    }

    public void adoptPosition(int x, int y) {
        this.x = x;
        this.y = y;
        setVisible(true);
    }

    public void move(int changeX, int changeY) {
        x += changeX;
        y += changeY;

        if (searchBox != null) {
            searchBox.setPosition(searchBox.getX() + changeX, searchBox.getY() + changeY);
        }
        if (magnifierIconPlacement != null) {
            magnifierIconPlacement = new ScreenRectangle(new ScreenPosition(
                  magnifierIconPlacement.left() + changeX,
                  magnifierIconPlacement.top() + changeY
            ), magnifierIconPlacement.width(), magnifierIconPlacement.height());
        }
        recipeBookPage.init(minecraft, x, y);
        recipeBookPage.updateArrowButtons();
        filterButton.setPosition(filterButton.getX() + changeX, filterButton.getY() + changeY);
        for (RecipeBookTabButton tabButton : this.tabButtons) {
            if (tabButton.visible) {
                tabButton.setPosition(tabButton.getX() + changeX, tabButton.getY() + changeY);
            }
        }
    }

    @Override
    protected void updateTabs(boolean isFiltering) {
        //Like super except adjusts these two variables to be set from the origin methods
        int xPosTab = getXOrigin() - 30;
        int yPosTab = getYOrigin() + 3;
        int yOffset = 27;
        int index = 0;

        for (RecipeBookTabButton tabButton : this.tabButtons) {
            ExtendedRecipeBookCategory category = tabButton.getCategory();
            if (category instanceof SearchRecipeBookCategory || RecipeBookManager.getSearchCategories().containsKey(category)) {
                tabButton.visible = true;
                tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
            } else if (tabButton.updateVisibility(this.book)) {
                tabButton.setPosition(xPosTab, yPosTab + yOffset * index++);
                tabButton.startAnimation(this.book, isFiltering);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //Copy of super but only renders a smaller part of the background so that it doesn't render the hard outside borders
        if (isVisible()) {
            if (!this.minecraft.hasControlDown()) {
                this.time += a;
            }

            int xo = this.getXOrigin();
            int yo = this.getYOrigin();
            int offset = 7;
            //Modified call to include an offset so that the full texture doesn't get rendered
            graphics.blit(RenderPipelines.GUI_TEXTURED, RECIPE_BOOK_LOCATION, xo + offset, yo + offset, 1 + offset, 1 + offset,
                  IMAGE_WIDTH - 2 * offset, IMAGE_HEIGHT - 2 * offset, 256, 256);
            if (this.searchBox != null) {
                this.searchBox.extractRenderState(graphics, mouseX, mouseY, a);
            }

            for (RecipeBookTabButton tabButton : this.tabButtons) {
                tabButton.extractRenderState(graphics, mouseX, mouseY, a);
            }

            this.filterButton.extractRenderState(graphics, mouseX, mouseY, a);
            this.recipeBookPage.extractRenderState(graphics, xo, yo, mouseX, mouseY, a);
        }
    }
}