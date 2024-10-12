package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.MekanismLang;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiOredictionificatorFilter extends GuiTextFilter<OredictionificatorItemFilter, TileEntityOredictionificator> {

    private static final int WIDTH = 162;

    public static GuiOredictionificatorFilter create(IGuiWrapper gui, TileEntityOredictionificator tile) {
        return new GuiOredictionificatorFilter(gui, (gui.getXSize() - WIDTH) / 2, 15, tile, null);
    }

    public static GuiOredictionificatorFilter edit(IGuiWrapper gui, TileEntityOredictionificator tile, OredictionificatorItemFilter filter) {
        return new GuiOredictionificatorFilter(gui, (gui.getXSize() - WIDTH) / 2, 15, tile, filter);
    }

    private GuiOredictionificatorFilter(IGuiWrapper gui, int x, int y, TileEntityOredictionificator tile, @Nullable OredictionificatorItemFilter origFilter) {
        super(gui, x, y, WIDTH, 100, MekanismLang.OREDICTIONIFICATOR_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected int getScreenHeight() {
        return 52;
    }

    @Override
    protected void init() {
        super.init();
        addChild(new MekanismImageButton(gui(), relativeX + 3, relativeY + 38, 12, getButtonLocation("left"), (element, mouseX, mouseY) -> {
            if (filter.hasFilter()) {
                filter.previous();
                slotDisplay.updateStackList();
                return true;
            }
            return false;
        })).setTooltip(MekanismLang.LAST_ITEM);
        addChild(new MekanismImageButton(gui(), relativeX + 16, relativeY + 38, 12, getButtonLocation("right"), (element, mouseX, mouseY) -> {
            if (filter.hasFilter()) {
                filter.next();
                slotDisplay.updateStackList();
                return true;
            }
            return false;
        })).setTooltip(MekanismLang.NEXT_ITEM);
    }

    @Override
    protected CharPredicate getInputValidator() {
        return InputValidator.RESOURCE_LOCATION;
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
            return false;
        }
        String modid = "c";
        String newFilter = name.toLowerCase(Locale.ROOT);
        if (newFilter.contains(":")) {
            String[] split = newFilter.split(":");
            modid = split[0];
            newFilter = split[1];
            if (modid.contains("/")) {
                filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_INVALID_NAMESPACE);
                return false;
            }
        }
        if (newFilter.contains(":")) {
            filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_INVALID_PATH);
            return false;
        }
        ResourceLocation filterLocation = ResourceLocation.fromNamespaceAndPath(modid, newFilter);
        if (filter.hasFilter() && filter.filterMatches(filterLocation)) {
            filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG);
        } else if (TileEntityOredictionificator.isValidTarget(filterLocation)) {
            filter.setFilter(filterLocation);
            slotDisplay.updateStackList();
            text.setText("");
            return true;
        } else {
            filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_UNSUPPORTED_TAG);
        }
        return false;
    }

    @Override
    protected List<Component> getScreenText() {
        List<Component> list = super.getScreenText();
        if (filter.hasFilter()) {
            ItemStack renderStack = slotDisplay.getRenderStack();
            if (!renderStack.isEmpty()) {
                list.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(renderStack, MekanismUtils.getModId(renderStack)));
            }
            list.add(TextComponentUtil.getString(filter.getFilterText()));
        }
        return list;
    }

    @Override
    protected OredictionificatorItemFilter createNewFilter() {
        return new OredictionificatorItemFilter();
    }

    @NotNull
    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack result = filter.getResult();
        return result.isEmpty() ? Collections.emptyList() : Collections.singletonList(result);
    }

    @Nullable
    @Override
    public GuiFilterSelect<TileEntityOredictionificator> getFilterSelect(IGuiWrapper gui, TileEntityOredictionificator tileEntityOredictionificator) {
        return null;
    }

    @Override
    public boolean hasFilterSelect() {
        return false;
    }
}