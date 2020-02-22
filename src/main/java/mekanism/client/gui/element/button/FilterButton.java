package mekanism.client.gui.element.button;

import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.HashList;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

//TODO: This almost seems more like it should be a more generic GuiElement, than a MekanismButton
public class FilterButton extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_holder.png");
    private static final int TEXTURE_WIDTH = 96;
    private static final int TEXTURE_HEIGHT = 58;

    private final Supplier<HashList<? extends IFilter<?>>> filters;
    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;
    private final IntSupplier filterIndex;
    private final GuiSlot slot;
    private final int index;

    public FilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          IntConsumer upButtonPress, IntConsumer downButtonPress, BiConsumer<IFilter<?>, Integer> onPress) {
        super(gui, gui.getLeft() + x, gui.getTop() + y, TEXTURE_WIDTH, 29, "", () -> onPress.accept(filters.get().get(filterIndex.getAsInt() + index), index), null);
        this.index = index;
        this.filterIndex = filterIndex;
        this.filters = filters;
        int arrowX = this.x + TEXTURE_WIDTH - 12;
        slot = new GuiSlot(SlotType.NORMAL, gui, x + 2, y + 2);
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
        } else  {
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        if (upButton.isMouseOver(mouseX, mouseY)) {
            upButton.renderToolTip(xAxis, yAxis);
        } else if (downButton.isMouseOver(mouseX, mouseY)) {
            downButton.renderToolTip(xAxis, yAxis);
        } else  {
            super.renderForeground(mouseX, mouseY, xAxis, yAxis);
            //TODO: render tooltip text
        }
    }

    private void setVisibility(boolean visible) {
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
        IFilter<?> filter = filterList.get(index);
        upButton.visible = filter != null && index > 0;
        downButton.visible = filter != null && index < filterList.size() - 1;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        setVisibility(filters.get().get(filterIndex.getAsInt() + index) != null);
        if (visible) {
            super.render(mouseX, mouseY, partialTicks);
            //Update visibility state of our buttons
            updateButtonVisibility();
            //Render our sub buttons and our slot
            upButton.render(mouseX, mouseY, partialTicks);
            downButton.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        IFilter<?> filter = filters.get().get(filterIndex.getAsInt() + index);
        if (filter instanceof IItemStackFilter) {
            MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
        } else if (filter instanceof ITagFilter) {
            MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
        } else if (filter instanceof IMaterialFilter) {
            MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
        } else if (filter instanceof IModIDFilter) {
            MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
        }
        minecraft.textureManager.bindTexture(TEXTURE);
        blit(x, y, 0, isMouseOver(mouseX, mouseY) ? 0 : height, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MekanismRenderer.resetColor();
        slot.renderButton(mouseX, mouseY, partialTicks);
    }
}