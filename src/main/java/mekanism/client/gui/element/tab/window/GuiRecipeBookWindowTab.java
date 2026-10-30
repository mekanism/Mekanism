package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiRecipeBookWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.recipe_book.VirtualRecipeBookComponent;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;

public class GuiRecipeBookWindowTab extends GuiWindowCreatorTab<VirtualRecipeBookComponent<?>, GuiRecipeBookWindowTab> {

    private static final SelectedWindowData WINDOW_DATA = new SelectedWindowData(WindowType.RECIPE_BOOK);

    private final boolean isPrecisionSawmill;

    public GuiRecipeBookWindowTab(IGuiWrapper gui, VirtualRecipeBookComponent<?> recipeBook, boolean isPrecisionSawmill, Supplier<GuiRecipeBookWindowTab> elementSupplier) {
        this(gui, recipeBook, false, 62, isPrecisionSawmill, elementSupplier);
    }

    public GuiRecipeBookWindowTab(IGuiWrapper gui, VirtualRecipeBookComponent<?> recipeBook, boolean left, int y, boolean isPrecisionSawmill, Supplier<GuiRecipeBookWindowTab> elementSupplier) {
        this.isPrecisionSawmill = isPrecisionSawmill;
        super(Mekanism.rl("button/recipe_book"), gui, recipeBook, left ? -26 : gui.getImageWidth(), y, 26, 18, left, elementSupplier);
        setTooltip(MekanismLang.RECIPE_BOOK);
    }

    @Override
    protected int getTabColor() {
        return SpecialColors.TAB_RECIPE_BOOK.argb();
    }

    @Override
    protected GuiWindow createWindow(SelectedWindowData windowData) {
        return new GuiRecipeBookWindow(gui(), (getGuiWidth() - GuiRecipeBookWindow.WIDTH_WITH_TABS) / 2, GuiRecipeBookWindow.HEIGHT, dataSource, isPrecisionSawmill, windowData);
    }

    @Override
    protected SelectedWindowData getNextWindowData() {
        return WINDOW_DATA;
    }

    @Override
    protected boolean wasPinned(SelectedWindowData windowData) {
        if (windowData.type() == WindowType.RECIPE_BOOK) {
            return dataSource.isVisibleAccordingToBookData();
        }
        return super.wasPinned(windowData);
    }
}