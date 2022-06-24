package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTagFilter<FILTER extends ITagFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiTextFilter<FILTER, TILE> {

    protected GuiTagFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.TAG_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected CharPredicate getInputValidator() {
        return InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS);
    }

    @Override
    protected List<Component> getScreenText() {
        List<Component> list = super.getScreenText();
        list.add(MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName()));
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.TAG_FILTER_NO_TAG;
    }

    @Override
    protected boolean setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            filterSaveFailed(getNoFilterSaveError());
        } else if (name.equals(filter.getTagName())) {
            filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG);
        } else if (!hasMatchingTargets(name)) {
            filterSaveFailed(MekanismLang.TEXT_FILTER_NO_MATCHES);
        } else {
            filter.setTagName(name);
            slotDisplay.updateStackList();
            text.setText("");
            filterSaveSuccess();
            return true;
        }
        return false;
    }

    protected boolean hasMatchingTargets(String name) {
        return !TagCache.getItemTagStacks(name).isEmpty();
    }

    @NotNull
    @Override
    protected List<ItemStack> getRenderStacks() {
        if (filter.hasFilter()) {
            return TagCache.getItemTagStacks(filter.getTagName());
        }
        return Collections.emptyList();
    }
}