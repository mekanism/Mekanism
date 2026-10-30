package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.display.slot.ChemicalConversionSlotDisplay;
import mekanism.common.recipe.display.slot.ChemicalTankSlotDisplay;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

public class RecipeViewerUtils {

    private RecipeViewerUtils() {
    }

    public static final IProgressInfoHandler CONSTANT_PROGRESS = () -> 1;
    public static final IBarInfoHandler FULL_BAR = () -> 1;

    public static IProgressInfoHandler progressHandler(int processTime) {
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return () -> {
            float subTime = Util.getMillis() % time;
            return subTime / time;
        };
    }

    public static IBarInfoHandler barProgressHandler(int processTime) {
        Component tooltip = MekanismLang.TICKS_REQUIRED.translate(processTime);
        int time = SharedConstants.MILLIS_PER_TICK * processTime;
        return new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return tooltip;
            }

            @Override
            public double getLevel() {
                double subTime = Util.getMillis() % time;
                return subTime / time;
            }
        };
    }

    public static <T extends @Nullable Object> T getCurrent(List<T> elements) {
        return elements.get(getIndex(elements));
    }

    private static int getIndex(List<?> elements) {
        return (int) (Util.getMillis() / TimeUtil.MILLISECONDS_PER_SECOND % elements.size());
    }

    public static SlotDisplay getStacksFor(ChemicalStackIngredient ingredient, boolean displayConversions) {
        SlotDisplay ingredientDisplay = ingredient.display();
        //Always include the chemical tank of the type to portray that we accept items
        SlotDisplay display = new ChemicalTankSlotDisplay(ingredientDisplay);
        if (displayConversions) {
            //See if there are any chemical to item mappings
            return new SlotDisplay.Composite(List.of(display, new ChemicalConversionSlotDisplay(ingredientDisplay)));
        }
        return display;
    }

    public static List<RecipeHolder<ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>>> getLiquificationRecipes() {
        //Copy the explicit recipes into a new list
        List<RecipeHolder<ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>>> recipes = new ArrayList<>(MekanismRecipeType.LIQUIFICATION.getRecipes());
        //TODO: Do we want to loop creative tabs or something instead?
        // In theory recipe loaders should init the creative tabs before we are called so we wouldn't need to call
        // CreativeModeTab#buildContents, and in theory we only need to care about things in search so could use:
        // CreativeModeTabs.searchTab().getDisplayItems(). The bigger issue is how to come up with unique synthetic
        // names for the recipes as EMI requires they be unique. (Maybe index them?)
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            RecipeHolder<ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>> recipeHolder = TileEntityNutritionalLiquifier.createDynamicRecipe(entry.getValue().getDefaultInstance());
            if (recipeHolder != null) {
                recipes.add(recipeHolder);
            }
        }
        return List.copyOf(recipes);
    }
}