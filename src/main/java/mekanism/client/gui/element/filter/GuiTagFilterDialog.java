package mekanism.client.gui.element.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiTagFilterDialog extends GuiTextFilterDialog<QIOTagFilter, TileEntityQIOFilterHandler> {

    public static GuiTagFilterDialog create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiTagFilterDialog(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiTagFilterDialog edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOTagFilter filter) {
        return new GuiTagFilterDialog(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiTagFilterDialog(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, QIOTagFilter origFilter) {
        super(gui, x, y, 152, 90, MekanismLang.TAG_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        list.add(MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName()));
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.TAG_FILTER_NO_TAG;
    }

    @Override
    public QIOTagFilter createNewFilter() {
        return new QIOTagFilter();
    }

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = getNoFilterSaveError().translateColored(EnumColor.DARK_RED);
            ticker = 20;
        } else if (name.equals(filter.getTagName())) {
            status = MekanismLang.TAG_FILTER_SAME_TAG.translateColored(EnumColor.DARK_RED);
            ticker = 20;
        } else {
            filter.setTagName(name);
            slotDisplay.updateStackList();
            text.setText("");
        }
    }

    @Override
    protected List<ItemStack> getRenderStacks() {
        if (filter.hasFilter()) {
            return TagCache.getItemTagStacks(filter.getTagName());
        }
        return Collections.emptyList();
    }
}
