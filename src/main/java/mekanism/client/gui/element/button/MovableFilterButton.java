package mekanism.client.gui.element.button;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MovableFilterButton extends FilterButton {

    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;
    private final IntConsumer upButtonPress;
    private final IntConsumer downButtonPress;

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, FilterManager<?> filterManager, IntConsumer upButtonPress,
          IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, IntConsumer toggleButtonPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        this(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filterManager, upButtonPress, downButtonPress, onPress, toggleButtonPress, renderStackSupplier);
    }

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex, FilterManager<?> filterManager,
          IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, IntConsumer toggleButtonPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, index, filterIndex, filterManager, onPress, toggleButtonPress, renderStackSupplier);
        int arrowX = relativeX + width - 14;
        this.upButtonPress = upButtonPress;
        this.downButtonPress = downButtonPress;
        upButton = addPositionOnlyChild(new FilterSelectButton(gui, arrowX, relativeY + (height / 2) - 8, false, (element, mouseX, mouseY) -> {
            MovableFilterButton self = (MovableFilterButton) element;
            self.upButtonPress.accept(self.getActualIndex());
            return true;
        }, (onHover, guiGraphics, mouseX, mouseY) -> onHover.displayTooltips(guiGraphics, mouseX, mouseY, MekanismLang.MOVE_UP.translate(), MekanismLang.MOVE_TO_TOP.translate())));
        downButton = addPositionOnlyChild(new FilterSelectButton(gui, arrowX, relativeY + (height / 2) + 1, true, (element, mouseX, mouseY) -> {
            MovableFilterButton self = (MovableFilterButton) element;
            self.downButtonPress.accept(self.getActualIndex());
            return true;
        }, (onHover, guiGraphics, mouseX, mouseY) -> onHover.displayTooltips(guiGraphics, mouseX, mouseY, MekanismLang.MOVE_DOWN.translate(), MekanismLang.MOVE_TO_BOTTOM.translate())));
    }

    @Override
    protected int getToggleXShift() {
        return 17;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (upButton.isValidClickButton(button) && upButton.isMouseOver(mouseX, mouseY)) {
            upButton.onClick(mouseX, mouseY, button);
        } else if (downButton.isValidClickButton(button) && downButton.isMouseOver(mouseX, mouseY)) {
            downButton.onClick(mouseX, mouseY, button);
        } else if (super.isValidClickButton(button)) {
            super.onClick(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean isValidClickButton(int button) {
        return super.isValidClickButton(button) || upButton.isValidClickButton(button) || downButton.isValidClickButton(button);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int xAxis = mouseX - getGuiLeft(), yAxis = mouseY - getGuiTop();
        if (upButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            upButton.renderToolTip(guiGraphics, xAxis, yAxis);
        } else if (downButton.isMouseOverCheckWindows(mouseX, mouseY)) {
            downButton.renderToolTip(guiGraphics, xAxis, yAxis);
        }
        super.renderForeground(guiGraphics, mouseX, mouseY);
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
        int index = getActualIndex();
        IFilter<?> filter = getFilter();
        upButton.visible = filter != null && index > 0;
        downButton.visible = filter != null && index < filterManager.count() - 1;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        IFilter<?> filter = getFilter();
        EnumColor color;
        if (filter instanceof IItemStackFilter) {
            color = EnumColor.INDIGO;
        } else if (filter instanceof ITagFilter) {
            color = EnumColor.BRIGHT_GREEN;
        } else if (filter instanceof IModIDFilter) {
            color = EnumColor.RED;
        } else {
            color = null;
        }
        if (color != null) {
            GuiUtils.fill(guiGraphics, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), MekanismRenderer.getColorARGB(color, 0.3F));
        }
        updateButtonVisibility();
        //Render our sub buttons and our slot
        upButton.onDrawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        downButton.onDrawBackground(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMaxLength() {
        return super.getMaxLength() - 12;
    }
}