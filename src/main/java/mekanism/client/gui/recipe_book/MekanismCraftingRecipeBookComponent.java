package mekanism.client.gui.recipe_book;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;

/// Copied from [CraftingRecipeBookComponent] but made to extend [VirtualRecipeBookComponent] as to support being part of a GUI window
public class MekanismCraftingRecipeBookComponent extends VirtualRecipeBookComponent<AbstractCraftingMenu> {

    public MekanismCraftingRecipeBookComponent(AbstractCraftingMenu menu, Supplier<Font> fontSupplier) {
        super(menu, fontSupplier, CraftingRecipeBookComponent.TABS, null);
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return this.menu.getResultSlot() == slot || this.menu.getInputGridSlots().contains(slot);
    }

    private boolean canDisplay(RecipeDisplay display) {
        int gridWidth = this.menu.getGridWidth();
        int gridHeight = this.menu.getGridHeight();
        return switch (display) {
            case ShapedCraftingRecipeDisplay shaped -> gridWidth >= shaped.width() && gridHeight >= shaped.height();
            case ShapelessCraftingRecipeDisplay shapeless -> gridWidth * gridHeight >= shapeless.ingredients().size();
            default -> false;
        };
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipe, ContextMap context) {
        ghostSlots.setResult(this.menu.getResultSlot(), context, recipe.result());
        switch (recipe) {
            case ShapedCraftingRecipeDisplay shaped -> {
                List<Slot> inputSlots = this.menu.getInputGridSlots();
                PlaceRecipeHelper.placeRecipe(
                    this.menu.getGridWidth(),
                    this.menu.getGridHeight(),
                    shaped.width(),
                    shaped.height(),
                    shaped.ingredients(),
                    (ingredient, gridIndex, _, _) -> {
                        Slot slot = inputSlots.get(gridIndex);
                        ghostSlots.setInput(slot, context, ingredient);
                    }
                );
            }
            case ShapelessCraftingRecipeDisplay shapeless -> {
                List<Slot> inputSlots = this.menu.getInputGridSlots();
                int slotCount = Math.min(shapeless.ingredients().size(), inputSlots.size());
                for (int i = 0; i < slotCount; i++) {
                    ghostSlots.setInput(inputSlots.get(i), context, shapeless.ingredients().get(i));
                }
            }
            default -> {
            }
        }
    }

    @Override
    protected WidgetSprites getFilterButtonTextures() {
        return CraftingRecipeBookComponent.FILTER_BUTTON_SPRITES;
    }

    @Override
    protected Component getRecipeFilterName() {
        return CraftingRecipeBookComponent.ONLY_CRAFTABLES_TOOLTIP;
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents stackedContents) {
        collection.selectRecipes(stackedContents, this::canDisplay);
    }
}