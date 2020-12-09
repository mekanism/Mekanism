package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.MekanismLang;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiOredictionificatorFilter extends GuiTextFilter<OredictionificatorFilter, TileEntityOredictionificator> {

    public static GuiOredictionificatorFilter create(IGuiWrapper gui, TileEntityOredictionificator tile) {
        return new GuiOredictionificatorFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiOredictionificatorFilter edit(IGuiWrapper gui, TileEntityOredictionificator tile, OredictionificatorFilter filter) {
        return new GuiOredictionificatorFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiOredictionificatorFilter(IGuiWrapper gui, int x, int y, TileEntityOredictionificator tile, OredictionificatorFilter origFilter) {
        super(gui, x, y, 152, 100, MekanismLang.OREDICTIONIFICATOR_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected int getScreenHeight() {
        return 53;
    }

    @Override
    protected int getSlotOffset() {
        return 32;
    }

    @Override
    protected void init() {
        super.init();
        addChild(new MekanismImageButton(guiObj, x + 10, y + 18, 12, getButtonLocation("left"), () -> {
            if (filter.hasFilter()) {
                filter.previous();
                slotDisplay.updateStackList();
            }
        }, getOnHover(MekanismLang.LAST_ITEM)));
        addChild(new MekanismImageButton(guiObj, x + 10, y + 52, 12, getButtonLocation("right"), () -> {
            if (filter.hasFilter()) {
                filter.next();
                slotDisplay.updateStackList();
            }
        }, getOnHover(MekanismLang.NEXT_ITEM)));
    }

    @Override
    protected CharPredicate getInputValidator() {
        return InputValidator.or(InputValidator.LETTER, InputValidator.DIGIT, InputValidator.from('_', ':', '/'));
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
            return;
        }
        String modid = "forge";
        String newFilter = name.toLowerCase(Locale.ROOT);
        if (newFilter.contains(":")) {
            String[] split = newFilter.split(":");
            modid = split[0];
            newFilter = split[1];
        }
        ResourceLocation filterLocation = new ResourceLocation(modid, newFilter);
        if (filter.hasFilter() && filter.filterMatches(filterLocation)) {
            filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG);
        } else {
            List<String> possibleFilters = TileEntityOredictionificator.possibleFilters.getOrDefault(modid, Collections.emptyList());
            if (possibleFilters.stream().anyMatch(newFilter::startsWith) && ItemTags.getCollection().getRegisteredTags().contains(filterLocation)) {
                filter.setFilter(filterLocation);
                slotDisplay.updateStackList();
                text.setText("");
            } else {
                filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_INCOMPATIBLE_TAG);
            }
        }
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
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
    protected OredictionificatorFilter createNewFilter() {
        return new OredictionificatorFilter();
    }

    @Nonnull
    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack result = filter.getResult();
        return result.isEmpty() ? Collections.emptyList() : Collections.singletonList(result);
    }
}