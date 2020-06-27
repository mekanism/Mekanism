package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.filter.IFilter;
import mekanism.common.lib.HashList;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

//TODO: This almost seems more like it should be a more generic GuiElement, than a MekanismButton
public class FilterButton extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_holder.png");
    protected static final int TEXTURE_WIDTH = 96;
    protected static final int TEXTURE_HEIGHT = 58;

    protected final Supplier<HashList<? extends IFilter<?>>> filters;
    protected final IntSupplier filterIndex;
    private final GuiSlot slot;
    protected final int index;

    public FilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          BiConsumer<IFilter<?>, Integer> onPress) {
        super(gui, gui.getLeft() + x, gui.getTop() + y, width, height, StringTextComponent.field_240750_d_,
              () -> onPress.accept(filters.get().getOrNull(filterIndex.getAsInt() + index), filterIndex.getAsInt() + index), null);
        this.index = index;
        this.filterIndex = filterIndex;
        this.filters = filters;
        slot = new GuiSlot(SlotType.NORMAL, gui, x + 2, y + 2);
    }

    protected void setVisibility(boolean visible) {
        this.field_230694_p_ = visible;
    }

    @Override
    public void func_230430_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        setVisibility(filters.get().getOrNull(filterIndex.getAsInt() + index) != null);
        super.func_230430_a_(matrix, mouseX, mouseY, partialTicks);
    }

    protected void colorButton() {
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        colorButton();
        minecraft.textureManager.bindTexture(TEXTURE);
        func_238466_a_(matrix, field_230690_l_, field_230691_m_, field_230688_j_, field_230689_k_, 0, isMouseOverCheckWindows(mouseX, mouseY) ? 0 : 29, TEXTURE_WIDTH, 29, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MekanismRenderer.resetColor();
        slot.func_230431_b_(matrix, mouseX, mouseY, partialTicks);
    }
}