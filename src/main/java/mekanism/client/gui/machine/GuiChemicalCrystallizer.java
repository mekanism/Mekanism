package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.merged.MergedChemicalTank.Current;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
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
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalCrystallizer extends GuiConfigurableTile<TileEntityChemicalCrystallizer, MekanismTileContainer<TileEntityChemicalCrystallizer>> {

    private final List<ItemStack> iterStacks = new ArrayList<>();
    private final IOreInfo oreInfo = new OreInfo();
    private GuiSequencedSlotDisplay slotDisplay;
    @Nonnull
    private Slurry prevSlurry = MekanismAPI.EMPTY_SLURRY;

    public GuiChemicalCrystallizer(MekanismTileContainer<TileEntityChemicalCrystallizer> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        titleLabelY = 4;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 157, 23));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addButton(new GuiMergedChemicalTankGauge<>(() -> tile.inputTank, () -> tile, GaugeType.STANDARD, this, 7, 4));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 53, 61).jeiCategory(tile));
        //Init slot display before gui screen, so it can reference it, but add it after, so it renders above it
        slotDisplay = new GuiSequencedSlotDisplay(this, 129, 14, () -> iterStacks);
        updateSlotContents();
        addButton(new GuiInnerScreen(this, 31, 13, 115, 42, () -> getScreenRenderStrings(this.oreInfo)));
        addButton(new GuiSlot(SlotType.ORE, this, 128, 13).setRenderAboveSlots());
        addButton(slotDisplay);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public void tick() {
        updateSlotContents();
        super.tick();
    }

    private void updateSlotContents() {
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (!boxedChemical.isEmpty() && boxedChemical.getChemicalType() == ChemicalType.SLURRY) {
            Slurry inputSlurry = (Slurry) boxedChemical.getChemicalStack().getType();
            if (prevSlurry != inputSlurry) {
                prevSlurry = inputSlurry;
                iterStacks.clear();
                if (!prevSlurry.isEmptyType() && !prevSlurry.isIn(MekanismTags.Slurries.DIRTY)) {
                    ITag<Item> oreTag = prevSlurry.getOreTag();
                    if (oreTag != null) {
                        for (Item ore : oreTag.getValues()) {
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

    public static List<ITextComponent> getScreenRenderStrings(IOreInfo oreInfo) {
        BoxedChemicalStack boxedChemical = oreInfo.getInputChemical();
        if (!boxedChemical.isEmpty()) {
            List<ITextComponent> ret = new ArrayList<>();
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

        @Nonnull
        BoxedChemicalStack getInputChemical();

        @Nullable
        ChemicalCrystallizerRecipe getRecipe();

        @Nonnull
        ItemStack getRenderStack();
    }

    private class OreInfo implements IOreInfo {

        private WeakReference<ChemicalCrystallizerRecipe> cachedRecipe;

        @Nonnull
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

        @Nonnull
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