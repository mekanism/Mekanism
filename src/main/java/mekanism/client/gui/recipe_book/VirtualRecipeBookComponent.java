package mekanism.client.gui.recipe_book;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.neoforged.neoforge.client.RecipeBookManager;
import net.neoforged.neoforge.client.recipebook.RecipeOverlayButtonCreator;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

//TODO - 26.4: Double check nothing changed that might have broken this, the overlay, or the ghost slots
public abstract class VirtualRecipeBookComponent<MENU extends RecipeBookMenu> extends RecipeBookComponent<MENU> implements IRecipeViewerIngredientHelper {

    private final Supplier<Font> fontSupplier;
    private int x;
    private int y;

    public VirtualRecipeBookComponent(MENU menu, Supplier<Font> fontSupplier, List<RecipeBookComponent.TabInfo> tabInfos, @Nullable RecipeOverlayButtonCreator overlayButtonCreator) {
        this.fontSupplier = fontSupplier;
        super(menu, tabInfos, MekanismRecipeButton::new, overlayButtonCreator);
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return slot instanceof InventoryContainerSlot inventorySlot && inventorySlot.isRecipeCraftingSlot();
    }

    @Override
    protected MekanismRecipeBookTabButton createTabButton(int x, int y, RecipeBookComponent.TabInfo tabInfo, Button.OnPress onPress) {
        return new MekanismRecipeBookTabButton(x, y, tabInfo, onPress);
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, @Nullable Slot hoveredSlot) {
        //TODO - 26.3: Allow extracting tooltips for the recipe book tabs
        if (isVisible()) {
            //Reimplementation of relevant recipe book page tooltip rendering
            if (this.minecraft.gui.screen() != null && this.recipeBookPage.hoveredButton instanceof MekanismRecipeButton recipeButton && !this.recipeBookPage.overlay.isVisible()) {
                switch (recipeButton.getGeneralDisplayStack()) {
                    case ItemStack stack -> {
                        Identifier tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);
                        graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY, stack, tooltipStyle);
                    }
                    case FluidStack stack -> {
                        Identifier tooltipStyle = stack.get(DataComponents.TOOLTIP_STYLE);
                        graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY, tooltipStyle);
                    }
                    case ChemicalStack stack -> graphics.setComponentTooltipForNextFrame(fontSupplier.get(), recipeButton.getTooltipText(stack), mouseX, mouseY);
                    default -> {
                    }
                }
            }
        }
    }

    protected void clearGhostValues() {
        this.ghostSlots.clear();
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay recipe) {
        clearGhostValues();
        super.fillGhostRecipe(recipe);
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

    public boolean isMouseOverTab(double mouseX, double mouseY) {
        for (RecipeBookTabButton tabButton : this.tabButtons) {
            //Note: is mouse over checks that it is visible
            if (tabButton.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        if (this.minecraft.gui.screen() != null && this.recipeBookPage.hoveredButton instanceof MekanismRecipeButton recipeButton && !this.recipeBookPage.overlay.isVisible()) {
            return recipeButton.getIngredient(mouseX, mouseY);
        }
        return Optional.empty();
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        if (this.minecraft.gui.screen() != null && this.recipeBookPage.hoveredButton instanceof MekanismRecipeButton recipeButton && !this.recipeBookPage.overlay.isVisible()) {
            return recipeButton.getIngredientBounds(mouseX, mouseY);
        }
        //Note: This should never be the case as we validated we had an ingredient but if it is just return the entire gui portion
        return new Rect2i(x, y, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    public void init(Minecraft minecraft) {
        init(0, 0, minecraft, false);
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
        //Copy of super but only renders a smaller part of the background so that it doesn't render the hard outside borders, and so it makes use of the origin methods
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