package mekanism.client.gui.element.window.filter;

import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public abstract class GuiFilterSelect<TILE extends TileEntityMekanism & ITileFilterHolder<?>> extends GuiWindow {

    private static final int FILTER_HEIGHT = 20;
    protected final TILE tile;

    protected GuiFilterSelect(IGuiWrapper gui, TILE tile, int filterCount) {
        super(gui, (gui.getXSize() - 152) / 2, 20, 152, 30 + filterCount * FILTER_HEIGHT, SelectedWindowData.UNSPECIFIED);
        this.tile = tile;
        addChild(new GuiElementHolder(gui, relativeX + 11, relativeY + 18, 130, 2 + filterCount * FILTER_HEIGHT));
        int buttonY = relativeY + 19;
        buttonY = addFilterButton(buttonY, MekanismLang.BUTTON_ITEMSTACK_FILTER, getItemStackFilterCreator());
        buttonY = addFilterButton(buttonY, MekanismLang.BUTTON_TAG_FILTER, getTagFilterCreator());
        addFilterButton(buttonY, MekanismLang.BUTTON_MODID_FILTER, getModIDFilterCreator());
    }

    private int addFilterButton(int buttonY, ILangEntry translationHelper, @Nullable GuiFilterCreator<TILE> filterSupplier) {
        if (filterSupplier == null) {
            return buttonY;
        }
        addChild(new TranslationButton(gui(), relativeX + 12, buttonY, 128, FILTER_HEIGHT, translationHelper, (element, mouseX, mouseY) -> {
            //Add the window for the filter dialog to the parent gui
            IGuiWrapper gui = element.gui();
            gui.addWindow(filterSupplier.create(gui, tile));
            //And close the filter select dialog
            return close(element, mouseX, mouseY);
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
    protected GuiFilterCreator<TILE> getModIDFilterCreator() {
        return null;
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawTitleText(guiGraphics, MekanismLang.CREATE_FILTER_TITLE.translate(), 6);
    }

    @FunctionalInterface
    protected interface GuiFilterCreator<TILE extends TileEntityMekanism & ITileFilterHolder<?>> {

        //Note: This needs to be a wildcard for the tile as we don't actually care about it in the return result,
        // and otherwise eclipse considers it a mismatched bounds error
        GuiFilter<?, ?> create(IGuiWrapper gui, TILE tile);
    }
}