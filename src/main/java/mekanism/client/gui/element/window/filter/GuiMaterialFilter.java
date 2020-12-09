package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIGhostTarget.IGhostBlockItemConsumer;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiMaterialFilter<FILTER extends IMaterialFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiMaterialFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, MekanismLang.MATERIAL_FILTER.translate(), tile, origFilter);
        if (filter.hasFilter()) {
            slotDisplay.updateStackList();
        }
    }

    @Override
    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = super.getScreenText();
        if (filter.hasFilter()) {
            list.add(MekanismLang.MATERIAL_FILTER_DETAILS.translate());
            list.add(filter.getMaterialItem().getDisplayName());
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
        if (filter.hasFilter()) {
            return TagCache.getMaterialStacks(filter.getMaterialItem());
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    protected IGhostBlockItemConsumer getGhostHandler() {
        return ingredient -> setFilterStack(StackUtils.size((ItemStack) ingredient, 1));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseClickSlot(getGuiObj(), button, mouseX, mouseY, relativeX + 8, relativeY + getSlotOffset() + 1, NOT_EMPTY_BLOCK, this::setFilterStack) ||
               super.mouseClicked(mouseX, mouseY, button);
    }

    private void setFilterStack(@Nonnull ItemStack stack) {
        filter.setMaterialItem(stack);
        slotDisplay.updateStackList();
        playClickSound();
    }
}