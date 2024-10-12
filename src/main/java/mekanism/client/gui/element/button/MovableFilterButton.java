package mekanism.client.gui.element.button;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MovableFilterButton extends FilterButton {

    private static final Tooltip MOVE_UP = TooltipUtils.create(MekanismLang.MOVE_UP, MekanismLang.MOVE_TO_TOP);
    private static final Tooltip MOVE_DOWN = TooltipUtils.create(MekanismLang.MOVE_DOWN, MekanismLang.MOVE_TO_BOTTOM);

    private final FilterSelectButton upButton;
    private final FilterSelectButton downButton;
    public MovableFilterButton(IGuiWrapper gui, int x, int y, int index, IntSupplier filterIndex, FilterManager<?> filterManager, IntConsumer upButtonPress,
          IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, IntConsumer toggleButtonPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        this(gui, x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT / 2, index, filterIndex, filterManager, upButtonPress, downButtonPress, onPress, toggleButtonPress, renderStackSupplier);
    }

    public MovableFilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex, FilterManager<?> filterManager,
          IntConsumer upButtonPress, IntConsumer downButtonPress, ObjIntConsumer<IFilter<?>> onPress, IntConsumer toggleButtonPress,
          Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, index, filterIndex, filterManager, onPress, toggleButtonPress, renderStackSupplier);
        int arrowX = relativeX + this.width - 14;
        int halfHeight = this.height / 2;
        upButton = addChild(new FilterSelectButton(gui, arrowX, relativeY + halfHeight - 8, false, (element, mouseX, mouseY) -> {
            upButtonPress.accept(getActualIndex());
            return true;
        }));
        upButton.setTooltip(MOVE_UP);
        downButton = addChild(new FilterSelectButton(gui, arrowX, relativeY + halfHeight + 1, true, (element, mouseX, mouseY) -> {
            downButtonPress.accept(getActualIndex());
            return true;
        }));
        downButton.setTooltip(MOVE_DOWN);
    }

    @Override
    protected int getToggleXShift() {
        return 17;
    }

    @Override
    protected void setVisibility(boolean visible) {
        super.setVisibility(visible);
        if (visible) {
            updateButtonVisibility(getFilter());
        } else {
            //Ensure the subcomponents are not marked as visible
            upButton.visible = false;
            downButton.visible = false;
        }
    }

    private void updateButtonVisibility(@Nullable IFilter<?> filter) {
        int index = getActualIndex();
        upButton.visible = filter != null && index > 0;
        downButton.visible = filter != null && index < filterManager.count() - 1;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        IFilter<?> filter = getFilter();
        EnumColor color = switch (filter) {
            case IItemStackFilter<?> stackFilter -> EnumColor.INDIGO;
            case ITagFilter<?> tagFilter -> EnumColor.BRIGHT_GREEN;
            case IModIDFilter<?> modIDFilter -> EnumColor.RED;
            case null, default -> null;
        };
        if (color != null) {
            GuiUtils.fill(guiGraphics, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), MekanismRenderer.getColorARGB(color, 0.3F));
        }
        updateButtonVisibility(filter);
    }
}