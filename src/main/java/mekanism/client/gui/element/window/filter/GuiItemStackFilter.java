package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiItemStackFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.ITEM_FILTER.translate(), tile, origFilter);
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        if (filter.hasFilter()) {
            list.add(filter.getItemStack().getDisplayName());
        }
        return list;
    }

    @Override
    protected ILangEntry getNoFilterSaveError() {
        return MekanismLang.ITEM_FILTER_NO_ITEM;
    }

    @Nonnull
    @Override
    protected List<ItemStack> getRenderStacks() {
        ItemStack stack = filter.getItemStack();
        return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
    }

    @Nullable
    @Override
    protected IGhostItemConsumer getGhostHandler() {
        return ingredient -> setFilterStack(StackUtils.size((ItemStack) ingredient, 1));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseClickSlot(gui(), button, mouseX, mouseY, relativeX + 8, relativeY + getSlotOffset() + 1, NOT_EMPTY, this::setFilterStack) ||
               super.mouseClicked(mouseX, mouseY, button);
    }

    private void setFilterStack(@Nonnull ItemStack stack) {
        filter.setItemStack(stack);
        slotDisplay.updateStackList();
        playClickSound();
    }
}