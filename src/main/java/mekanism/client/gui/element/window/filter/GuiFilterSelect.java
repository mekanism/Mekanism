package mekanism.client.gui.element.window.filter;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;

public abstract class GuiFilterSelect extends GuiWindow {

    protected GuiFilterSelect(IGuiWrapper gui) {
        super(gui, (gui.getWidth() - 152) / 2, 20, 152, 110);
        addChild(new GuiElementHolder(gui, 23, relativeY + 18, 130, 82));
        addChild(new TranslationButton(gui, gui.getLeft() + 24, this.y + 19, 128, 20, MekanismLang.BUTTON_ITEMSTACK_FILTER,
              () -> openCreateFilterDialog(createNewItemStackFilter())));
        addChild(new TranslationButton(gui, gui.getLeft() + 24, this.y + 39, 128, 20, MekanismLang.BUTTON_TAG_FILTER,
              () -> openCreateFilterDialog(createNewTagFilter())));
        addChild(new TranslationButton(gui, gui.getLeft() + 24, this.y + 59, 128, 20, MekanismLang.BUTTON_MATERIAL_FILTER,
              () -> openCreateFilterDialog(createNewMaterialFilter())));
        addChild(new TranslationButton(gui, gui.getLeft() + 24, this.y + 79, 128, 20, MekanismLang.BUTTON_MODID_FILTER,
              () -> openCreateFilterDialog(createNewModIDFilter())));
    }

    protected abstract GuiFilter<? extends IItemStackFilter<?>, ?> createNewItemStackFilter();

    protected abstract GuiFilter<? extends ITagFilter<?>, ?> createNewTagFilter();

    protected abstract GuiFilter<? extends IMaterialFilter<?>, ?> createNewMaterialFilter();

    protected abstract GuiFilter<? extends IModIDFilter<?>, ?> createNewModIDFilter();

    private void openCreateFilterDialog(GuiFilter<?, ?> filterDialog) {
        //Add the window for the filter dialog to the parent gui
        gui().addWindow(filterDialog);
        //And close the filter select dialog
        close();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawString(matrix, MekanismLang.CREATE_FILTER_TITLE.translate(), relativeX + 38, relativeY + 6, titleTextColor());
    }
}