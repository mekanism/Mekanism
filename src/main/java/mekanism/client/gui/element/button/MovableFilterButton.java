package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.lib.HashList;
import net.minecraft.item.ItemStack;

public class MovableFilterButton extends FilterButton {

    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;
    private final GuiSequencedSlotDisplay slotDisplay;
    private IFilter<?> prevFilter;

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        this(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filters, upButtonPress, downButtonPress, onPress, renderStackSupplier);
    }

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex,
          Supplier<HashList<? extends IFilter<?>>> filters, IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, index, filterIndex, filters, onPress);
        int arrowX = this.x + width - 12;
        upButton = new FilterSelectButton(gui, arrowX, this.y + 1, false, () -> upButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_UP.translate(), xAxis, yAxis));
        downButton = new FilterSelectButton(gui, arrowX, this.y + height - 8, true, () -> downButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_DOWN.translate(), xAxis, yAxis));
        addChild(slotDisplay = new GuiSequencedSlotDisplay(gui, x + 3, y + 3,
              () -> renderStackSupplier.apply(filters.get().getOrNull(filterIndex.getAsInt() + index))));
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
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
        if (upButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            upButton.renderToolTip(matrix, xAxis, yAxis);
        } else if (downButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            downButton.renderToolTip(matrix, xAxis, yAxis);
        } else {
            super.renderForeground(matrix, mouseX, mouseY);
        }
        IFilter<?> filter = filters.get().getOrNull(filterIndex.getAsInt() + index);
        if (filter != prevFilter) {
            slotDisplay.updateStackList();
            prevFilter = filter;
        }
        int x = this.x - getGuiLeft();
        int y = this.y - getGuiTop();
        if (filter instanceof IItemStackFilter) {
            drawTextScaledBound(matrix, MekanismLang.ITEM_FILTER.translate(), x + 22, y + 2, titleTextColor(), 60);
        } else if (filter instanceof ITagFilter) {
            drawTextScaledBound(matrix, MekanismLang.TAG_FILTER.translate(), x + 22, y + 2, titleTextColor(), 60);
        } else if (filter instanceof IMaterialFilter) {
            drawTextScaledBound(matrix, MekanismLang.MATERIAL_FILTER.translate(), x + 22, y + 2, titleTextColor(), 60);
        } else if (filter instanceof IModIDFilter) {
            drawTextScaledBound(matrix, MekanismLang.MODID_FILTER.translate(), x + 22, y + 2, titleTextColor(), 60);
        }
        if (filter instanceof SorterFilter) {
            SorterFilter<?> sorterFilter = (SorterFilter<?>) filter;
            drawString(matrix, sorterFilter.color == null ? MekanismLang.NONE.translate() : sorterFilter.color.getColoredName(), x + 22, y + 11, titleTextColor());
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
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (visible) {
            //Update visibility state of our buttons
            updateButtonVisibility();
            //Render our sub buttons and our slot
            upButton.onDrawBackground(matrix, mouseX, mouseY, partialTicks);
            downButton.onDrawBackground(matrix, mouseX, mouseY, partialTicks);
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