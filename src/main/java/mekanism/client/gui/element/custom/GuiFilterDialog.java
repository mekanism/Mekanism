package mekanism.client.gui.element.custom;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiWindow;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterDialog<FILTER extends IFilter<FILTER>> extends GuiWindow {

    protected ITextComponent status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
    protected FILTER origFilter;
    protected FILTER filter;
    protected boolean isNew;
    protected int ticker;

    private final ITextComponent filterName;

    public GuiFilterDialog(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent filterName, FILTER origFilter) {
        super(gui, x, y, width, height);
        this.origFilter = origFilter;
        this.filterName = filterName;

        if (origFilter == null) {
            isNew = true;
            filter = createNewFilter();
        } else {
            filter = origFilter.clone();
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(filterName), relativeX + 39, relativeY + 6, titleTextColor());
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    public abstract FILTER createNewFilter();
}
