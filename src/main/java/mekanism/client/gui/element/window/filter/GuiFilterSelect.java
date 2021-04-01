package mekanism.client.gui.element.window.filter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;

public abstract class GuiFilterSelect<TILE extends TileEntityMekanism & ITileFilterHolder<?>> extends GuiWindow {

    private static final int FILTER_HEIGHT = 20;
    protected final TILE tile;

    protected GuiFilterSelect(IGuiWrapper gui, TILE tile, int filterCount) {
        super(gui, (gui.getWidth() - 152) / 2, 20, 152, 30 + filterCount * FILTER_HEIGHT, SelectedWindowData.UNSPECIFIED);
        this.tile = tile;
        addChild(new GuiElementHolder(gui, 23, relativeY + 18, 130, 2 + filterCount * FILTER_HEIGHT));
        int buttonY = this.y + 19;
        buttonY = addFilterButton(buttonY, MekanismLang.BUTTON_ITEMSTACK_FILTER, getItemStackFilterCreator());
        buttonY = addFilterButton(buttonY, MekanismLang.BUTTON_TAG_FILTER, getTagFilterCreator());
        buttonY = addFilterButton(buttonY, MekanismLang.BUTTON_MATERIAL_FILTER, getMaterialFilterCreator());
        addFilterButton(buttonY, MekanismLang.BUTTON_MODID_FILTER, getModIDFilterCreator());
    }

    private int addFilterButton(int buttonY, ILangEntry translationHelper, @Nullable GuiFilterCreator<TILE> filterSupplier) {
        if (filterSupplier == null) {
            return buttonY;
        }
        addChild(new TranslationButton(gui(), gui().getLeft() + 24, buttonY, 128, FILTER_HEIGHT, translationHelper, () -> {
            //Add the window for the filter dialog to the parent gui
            gui().addWindow(filterSupplier.create(gui(), tile));
            //And close the filter select dialog
            close();
        }));
        return buttonY + FILTER_HEIGHT;
    }

    @Nullable
    protected GuiFilterCreator<TILE> getItemStackFilterCreator() {
        return null;
    }

    @Nullable
    protected GuiFilterCreator<TILE> getTagFilterCreator() {
        return null;
    }

    @Nullable
    protected GuiFilterCreator<TILE> getMaterialFilterCreator() {
        return null;
    }

    @Nullable
    protected GuiFilterCreator<TILE> getModIDFilterCreator() {
        return null;
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawString(matrix, MekanismLang.CREATE_FILTER_TITLE.translate(), relativeX + 38, relativeY + 6, titleTextColor());
    }

    @FunctionalInterface
    protected interface GuiFilterCreator<TILE extends TileEntityMekanism & ITileFilterHolder<?>> {

        GuiFilter<?, TILE> create(IGuiWrapper gui, TILE tile);
    }
}