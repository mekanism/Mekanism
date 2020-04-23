package mekanism.client.gui.element.button;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.lib.HashList;

public class MovableFilterButton extends FilterButton {

    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          IntConsumer upButtonPress, IntConsumer downButtonPress, BiConsumer<IFilter<?>, Integer> onPress) {
        super(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filters, onPress);
        int arrowX = this.x + TEXTURE_WIDTH - 12;
        upButton = new FilterSelectButton(gui, arrowX, this.y + 1, false, () -> upButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, xAxis, yAxis) -> displayTooltip(MekanismLang.MOVE_UP.translate(), xAxis, yAxis));
        downButton = new FilterSelectButton(gui, arrowX, this.y + 21, true, () -> downButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, xAxis, yAxis) -> displayTooltip(MekanismLang.MOVE_DOWN.translate(), xAxis, yAxis));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (upButton.isMouseOver(mouseX, mouseY)) {
            upButton.onClick(mouseX, mouseY);
        } else if (downButton.isMouseOver(mouseX, mouseY)) {
            downButton.onClick(mouseX, mouseY);
        } else {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        if (upButton.isMouseOver(mouseX, mouseY)) {
            upButton.renderToolTip(xAxis, yAxis);
        } else if (downButton.isMouseOver(mouseX, mouseY)) {
            downButton.renderToolTip(xAxis, yAxis);
        } else {
            super.renderForeground(mouseX, mouseY, xAxis, yAxis);
        }
        int x = this.x - guiObj.getLeft();
        int y = this.y - guiObj.getTop();
        IFilter<?> filter = filters.get().getOrNull(filterIndex.getAsInt() + index);
        if (filter instanceof IItemStackFilter) {
            renderScaledText(MekanismLang.ITEM_FILTER.translate(), x + 22, y + 2, 0x404040, 60);
        } else if (filter instanceof ITagFilter) {
            renderScaledText(MekanismLang.TAG_FILTER.translate(), x + 22, y + 2, 0x404040, 60);
        } else if (filter instanceof IMaterialFilter) {
            renderScaledText(MekanismLang.MATERIAL_FILTER.translate(), x + 22, y + 2, 0x404040, 60);
        } else if (filter instanceof IModIDFilter) {
            renderScaledText(MekanismLang.MODID_FILTER.translate(), x + 22, y + 2, 0x404040, 60);
        }
        if (filter instanceof TransporterFilter<?>) {
            TransporterFilter<?> sorterFilter = (TransporterFilter<?>) filter;
            drawString(sorterFilter.color == null ? MekanismLang.NONE.translate() : sorterFilter.color.getColoredName(), x + 22, y + 11, 0x404040);
        }
    }

    @Override
    protected void setVisibility(boolean visible) {
        this.visible = visible;
        if (visible) {
            updateButtonVisibility();
        } else {
            //Ensure the sub components are not marked as visible
            upButton.visible = false;
            downButton.visible = false;
        }
    }

    private void updateButtonVisibility() {
        int index = filterIndex.getAsInt() + this.index;
        HashList<? extends IFilter<?>> filterList = filters.get();
        IFilter<?> filter = filterList.getOrNull(index);
        upButton.visible = filter != null && index > 0;
        downButton.visible = filter != null && index < filterList.size() - 1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (visible) {
            //Update visibility state of our buttons
            updateButtonVisibility();
            //Render our sub buttons and our slot
            upButton.render(mouseX, mouseY, partialTicks);
            downButton.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void colorButton() {
        IFilter<?> filter = filters.get().getOrNull(filterIndex.getAsInt() + index);
        if (filter instanceof IItemStackFilter) {
            MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
        } else if (filter instanceof ITagFilter) {
            MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
        } else if (filter instanceof IMaterialFilter) {
            MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
        } else if (filter instanceof IModIDFilter) {
            MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
        }
    }
}