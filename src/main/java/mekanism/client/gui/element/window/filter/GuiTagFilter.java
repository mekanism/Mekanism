package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTagFilter<FILTER extends ITagFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiTextFilter<FILTER, TILE> {

    protected GuiTagFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.TAG_FILTER.translate(), tile, origFilter);
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
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            filterSaveFailed(getNoFilterSaveError());
        } else if (name.equals(filter.getTagName())) {
            filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG);
        } else {
            filter.setTagName(name);
            slotDisplay.updateStackList();
            text.setText("");
        }
    }

    @Nonnull
    @Override
    protected List<ItemStack> getRenderStacks() {
        if (filter.hasFilter()) {
            return TagCache.getItemTagStacks(filter.getTagName());
        }
        return Collections.emptyList();
    }
}