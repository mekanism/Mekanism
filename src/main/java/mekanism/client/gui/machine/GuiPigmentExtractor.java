package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.lang.ref.WeakReference;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.bar.GuiHorizontalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiPigmentGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.GuiProgress.ColorDetails;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiPigmentExtractor extends GuiConfigurableTile<TileEntityPigmentExtractor, MekanismTileContainer<TileEntityPigmentExtractor>> {

    public GuiPigmentExtractor(MekanismTileContainer<TileEntityPigmentExtractor> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiHorizontalPowerBar(this, tile.getEnergyContainer(), 115, 75));
        addButton(new GuiEnergyTab(this, tile.getEnergyContainer(), tile::getActive));
        addButton(new GuiPigmentGauge(() -> tile.pigmentTank, () -> tile.getPigmentTanks(null), GaugeType.STANDARD, this, 131, 13));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 64, 40).jeiCategory(tile).colored(new PigmentColorDetails()));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    private class PigmentColorDetails implements ColorDetails {

        private WeakReference<ItemStackToPigmentRecipe> cachedRecipe;

        @Override
        public int getColorFrom() {
            return 0xFFFFFFFF;
        }

        @Override
        public int getColorTo() {
            if (tile == null) {
                //Should never actually be null, but just in case check it to make intellij happy
                return 0xFFFFFFFF;
            }
            if (tile.pigmentTank.isEmpty()) {
                //If the pigment tank is empty, try looking up the recipe and grabbing the color from it
                IInventorySlot inputSlot = tile.getInputSlot();
                if (!inputSlot.isEmpty()) {
                    ItemStack input = inputSlot.getStack();
                    ItemStackToPigmentRecipe recipe;
                    if (cachedRecipe == null) {
                        recipe = getRecipeAndCache();
                    } else {
                        recipe = cachedRecipe.get();
                        if (recipe == null || !recipe.getInput().testType(input)) {
                            recipe = getRecipeAndCache();
                        }
                    }
                    if (recipe != null) {
                        return getColor(recipe.getOutput(input).getChemicalColorRepresentation());
                    }
                }
                return 0xFFFFFFFF;
            }
            return getColor(tile.pigmentTank.getType().getColorRepresentation());
        }

        private ItemStackToPigmentRecipe getRecipeAndCache() {
            ItemStackToPigmentRecipe recipe = tile.getRecipe(0);
            if (recipe == null) {
                cachedRecipe = null;
            } else {
                cachedRecipe = new WeakReference<>(recipe);
            }
            return recipe;
        }

        private int getColor(int tint) {
            if ((tint & 0xFF000000) == 0) {
                return 0xFF000000 | tint;
            }
            return tint;
        }
    }
}