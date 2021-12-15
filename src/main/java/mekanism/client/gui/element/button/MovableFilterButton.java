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
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.collection.HashList;
import net.minecraft.item.ItemStack;

public class MovableFilterButton extends FilterButton {

    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        this(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filters, upButtonPress, downButtonPress, onPress, renderStackSupplier);
    }

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex,
          Supplier<HashList<? extends IFilter<?>>> filters, IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, index, filterIndex, filters, onPress, renderStackSupplier);
        int arrowX = relativeX + width - 12;
        upButton = addPositionOnlyChild(new FilterSelectButton(gui, arrowX, relativeY + 1, false, () -> upButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_UP.translate(), xAxis, yAxis)));
        downButton = addPositionOnlyChild(new FilterSelectButton(gui, arrowX, relativeY + height - 8, true,
              () -> downButtonPress.accept(index + filterIndex.getAsInt()),
              (onHover, matrix, xAxis, yAxis) -> displayTooltip(matrix, MekanismLang.MOVE_DOWN.translate(), xAxis, yAxis)));
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
        }
        super.renderForeground(matrix, mouseX, mouseY);
    }

    @Override
    protected void setVisibility(boolean visible) {
        super.setVisibility(visible);
        if (visible) {
            updateButtonVisibility();
        } else {
            //Ensure the subcomponents are not marked as visible
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
        IFilter<?> filter = getFilter(filters, filterIndex, index);
        EnumColor color;
        if (filter instanceof IItemStackFilter) {
            color = EnumColor.INDIGO;
        } else if (filter instanceof ITagFilter) {
            color = EnumColor.BRIGHT_GREEN;
        } else if (filter instanceof IMaterialFilter) {
            color = EnumColor.PINK;
        } else if (filter instanceof IModIDFilter) {
            color = EnumColor.RED;
        } else {
            color = null;
        }
        if (color != null) {
            GuiUtils.fill(matrix, x, y, width, height, MekanismRenderer.getColorARGB(color, 0.5F));
            MekanismRenderer.resetColor();
        }
        updateButtonVisibility();
        //Render our sub buttons and our slot
        upButton.onDrawBackground(matrix, mouseX, mouseY, partialTicks);
        downButton.onDrawBackground(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMaxLength() {
        return super.getMaxLength() - 12;
    }
}