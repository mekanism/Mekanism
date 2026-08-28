package mekanism.client.recipe_viewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.recipes.basic.BasicItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.display.slot.ChemicalConversionSlotDisplay;
import mekanism.common.recipe.display.slot.ChemicalTankSlotDisplay;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.util.RegistryUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
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

    public static long getCurrent(int[] elements) {
        return elements[getIndex(elements)];
    }

    private static int getIndex(int[] elements) {
        return (int) (Util.getMillis() / TimeUtil.MILLISECONDS_PER_SECOND % elements.length);
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

    public static Map<Identifier, BasicItemStackToFluidOptionalItemRecipe> getLiquificationRecipes() {
        Map<Identifier, BasicItemStackToFluidOptionalItemRecipe> liquification = new HashMap<>();
        //TODO: Do we want to loop creative tabs or something instead?
        // In theory recipe loaders should init the creative tabs before we are called so we wouldn't need to call
        // CreativeModeTab#buildContents, and in theory we only need to care about things in search so could use:
        // CreativeModeTabs.searchTab().getDisplayItems(). The bigger issue is how to come up with unique synthetic
        // names for the recipes as EMI requires they be unique. (Maybe index them?)
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            BasicItemStackToFluidOptionalItemRecipe recipe = TileEntityNutritionalLiquifier.getRecipe(entry.getValue().getDefaultInstance());
            if (recipe != null) {
                liquification.put(RegistryUtils.synthetic(entry.getKey().identifier(), "liquification", Mekanism.MODID), recipe);
            }
        }
        return liquification;
    }
}