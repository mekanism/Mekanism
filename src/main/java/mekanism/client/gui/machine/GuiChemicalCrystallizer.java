package mekanism.client.gui.machine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiMergedChemicalTankGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiChemicalCrystallizer extends GuiConfigurableTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {

    private final List<ItemStack> iterStacks = new ArrayList<>();
    private final IOreInfo oreInfo = new OreInfo();
    private GuiSequencedSlotDisplay slotDisplay;
    @NotNull
    private Slurry prevSlurry = MekanismAPI.EMPTY_SLURRY;

    public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 23))
              .warning(WarningType.NOT_ENOUGH_ENERGY, tile.getWarningCheck(RecipeError.NOT_ENOUGH_ENERGY));
        addRenderableWidget(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addRenderableWidget(new GuiMergedChemicalTankGauge<>(() -> tile.inputTank, () -> tile, GaugeType.STANDARD, this, 7, 4))
              .warning(WarningType.NO_MATCHING_RECIPE, tile.getWarningCheck(RecipeError.NOT_ENOUGH_INPUT));
        addRenderableWidget(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61).recipeViewerCategory(tile))
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, tile.getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        //Init slot display before gui screen, so it can reference it, but add it after, so it renders above it
        slotDisplay = new GuiSequencedSlotDisplay(this, 129, 14, () -> iterStacks);
        updateSlotContents();
        addRenderableWidget(new GuiInnerScreen(this, 31, 13, 115, 42, () -> getScreenRenderStrings(this.oreInfo)));
        addRenderableWidget(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
        addRenderableWidget(slotDisplay);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void containerTick() {
        updateSlotContents();
        super.containerTick();
    }

    private void updateSlotContents() {
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (!boxedChemical.isEmpty() && boxedChemical.getChemicalType() == ChemicalType.SLURRY) {
            Slurry inputSlurry = (Slurry) boxedChemical.getChemicalStack().getChemical();
            if (prevSlurry != inputSlurry) {
                prevSlurry = inputSlurry;
                iterStacks.clear();
                if (!prevSlurry.isEmptyType() && !prevSlurry.is(MekanismAPITags.Slurries.DIRTY)) {
                    TagKey<Item> oreTag = prevSlurry.getOreTag();
                    if (oreTag != null) {
                        for (Holder<Item> ore : BuiltInRegistries.ITEM.getTagOrEmpty(oreTag)) {
                            iterStacks.add(new ItemStack(ore));
                        }
                    }
                }
                slotDisplay.updateStackList();
            }
        } else if (!prevSlurry.isEmptyType()) {
            prevSlurry = MekanismAPI.EMPTY_SLURRY;
            iterStacks.clear();
            slotDisplay.updateStackList();
        }
    }

    public static List<Component> getScreenRenderStrings(IOreInfo oreInfo) {
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (!boxedChemical.isEmpty()) {
            List<Component> ret = new ArrayList<>();
            ret.add(boxedChemical.getTextComponent());
            if (boxedChemical.getChemicalType() == ChemicalType.SLURRY && !oreInfo.getRenderStack().isEmpty()) {
                ret.add(MekanismLang.GENERIC_PARENTHESIS.translate(oreInfo.getRenderStack()));
            } else {
                ChemicalCrystallizerRecipe recipe = oreInfo.getRecipe();
                if (recipe == null) {
                    ret.add(MekanismLang.NO_RECIPE.translate());
                } else {
                    ret.add(MekanismLang.GENERIC_PARENTHESIS.translate(recipe.getOutput(boxedChemical)));
                }
            }
            return ret;
        }
        return Collections.emptyList();
    }

    public interface IOreInfo {

        @NotNull
        BoxedChemicalStack getInputChemical();

        @Nullable
        ChemicalCrystallizerRecipe getRecipe();

        @NotNull
        ItemStack getRenderStack();
    }

    private class OreInfo implements IOreInfo {

        private WeakReference<ChemicalCrystallizerRecipe> cachedRecipe;

        @NotNull
        @Override
        public BoxedChemicalStack getInputChemical() {
            Current current = tile.inputTank.getCurrent();
            return current == Current.EMPTY ? BoxedChemicalStack.EMPTY : BoxedChemicalStack.box(tile.inputTank.getTankFromCurrent(current).getStack());
        }

        @Nullable
        @Override
        public ChemicalCrystallizerRecipe getRecipe() {
            BoxedChemicalStack input = getInputChemical();
            if (input.isEmpty()) {
                return null;
            }
            ChemicalCrystallizerRecipe recipe;
            if (cachedRecipe == null) {
                recipe = getRecipeAndCache();
            } else {
                recipe = cachedRecipe.get();
                if (recipe == null || !recipe.testType(input)) {
                    recipe = getRecipeAndCache();
                }
            }
            return recipe;
        }

        @NotNull
        @Override
        public ItemStack getRenderStack() {
            return slotDisplay == null ? ItemStack.EMPTY : slotDisplay.getRenderStack();
        }

        private ChemicalCrystallizerRecipe getRecipeAndCache() {
            ChemicalCrystallizerRecipe recipe = tile.getRecipe(0);
            if (recipe == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = new WeakReference<>(recipe);
            }
            return recipe;
        }
    }
}