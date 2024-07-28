package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget.IGhostItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiItemStackFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.ITEM_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected List<Component> getScreenText() {
        List<Component> list = super.getScreenText();
        if (filter.hasFilter()) {
            list.add(filter.getItemStack().getHoverName());
        }
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.ITEM_FILTER_NO_ITEM;
    }

    @NotNull
    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack stack = filter.getItemStack();
        return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    @Nullable
    @Override
    protected IGhostItemConsumer getGhostHandler() {
        return ingredient -> setFilterStackWithSound(((ItemStack) ingredient).copyWithCount(1));
    }

    @Nullable
    @Override
    protected IClickable getSlotClickHandler() {
        return getHandleClickSlot(NOT_EMPTY, this::setFilterStack);
    }

    private void setFilterStack(@NotNull ItemStack stack) {
        filter.setItemStack(stack);
        slotDisplay.updateStackList();
    }

    protected void setFilterStackWithSound(@NotNull ItemStack stack) {
        setFilterStack(stack);
        playClickSound(BUTTON_CLICK_SOUND);
    }
}