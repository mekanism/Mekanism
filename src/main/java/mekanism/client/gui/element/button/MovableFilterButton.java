package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
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
          IntConsumer upButtonPress, IntConsumer downButtonPress, BiConsumer<IFilter<?>, Integer> onPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        this(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filters, upButtonPress, downButtonPress, onPress, renderStackSupplier);
    }

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex,
          Supplier<HashList<? extends IFilter<?>>> filters, IntConsumer upButtonPress, IntConsumer downButtonPress, BiConsumer<IFilter<?>, Integer> onPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, index, filterIndex, filters, onPress);
        int arrowX = this.field_230690_l_ + width - 12;
        upButton = new FilterSelectButton(gui, arrowX, this.field_230691_m_ + 1, false, () -> upButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_UP.translate(), xAxis, yAxis));
        downButton = new FilterSelectButton(gui, arrowX, this.field_230691_m_ + height - 8, true, () -> downButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_DOWN.translate(), xAxis, yAxis));
        addChild(slotDisplay = new GuiSequencedSlotDisplay(gui, x + 3, y + 3,
              () -> renderStackSupplier.apply(filters.get().getOrNull(filterIndex.getAsInt() + index))));
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        if (upButton.func_231047_b_(mouseX, mouseY)) {
            upButton.func_230982_a_(mouseX, mouseY);
        } else if (downButton.func_231047_b_(mouseX, mouseY)) {
            downButton.func_230982_a_(mouseX, mouseY);
        } else {
            super.func_230982_a_(mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        int xAxis = mouseX - guiObj.getLeft(), yAxis = mouseY - guiObj.getTop();
        if (upButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            upButton.func_230443_a_(matrix, xAxis, yAxis);
        } else if (downButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            downButton.func_230443_a_(matrix, xAxis, yAxis);
        } else {
            super.renderForeground(matrix, mouseX, mouseY);
        }
        IFilter<?> filter = filters.get().getOrNull(filterIndex.getAsInt() + index);
        if (filter != prevFilter) {
            slotDisplay.updateStackList();
            prevFilter = filter;
        }
        int x = this.field_230690_l_ - guiObj.getLeft();
        int y = this.field_230691_m_ - guiObj.getTop();
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
        this.field_230694_p_ = visible;
        if (visible) {
            updateButtonVisibility();
        } else {
            //Ensure the sub components are not marked as visible
            upButton.field_230694_p_ = false;
            downButton.field_230694_p_ = false;
        }
    }

    private void updateButtonVisibility() {
        int index = filterIndex.getAsInt() + this.index;
        HashList<? extends IFilter<?>> filterList = filters.get();
        IFilter<?> filter = filterList.getOrNull(index);
        upButton.field_230694_p_ = filter != null && index > 0;
        downButton.field_230694_p_ = filter != null && index < filterList.size() - 1;
    }

    @Override
    public void func_230430_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.func_230430_a_(matrix, mouseX, mouseY, partialTicks);
        if (field_230694_p_) {
            //Update visibility state of our buttons
            updateButtonVisibility();
            //Render our sub buttons and our slot
            upButton.func_230430_a_(matrix, mouseX, mouseY, partialTicks);
            downButton.func_230430_a_(matrix, mouseX, mouseY, partialTicks);
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