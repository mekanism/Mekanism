package mekanism.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.collection.HashList;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterButton extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_holder.png");
    protected static final int TEXTURE_WIDTH = 96;
    protected static final int TEXTURE_HEIGHT = 58;

    protected final Supplier<HashList<? extends IFilter<?>>> filters;
    protected final GuiSequencedSlotDisplay slotDisplay;
    protected final IntSupplier filterIndex;
    private final GuiSlot slot;
    protected final int index;
    private IFilter<?> prevFilter;

    @Nullable
    protected static IFilter<?> getFilter(Supplier<HashList<? extends IFilter<?>>> filters, IntSupplier filterIndex, int index) {
        return filters.get().getOrNull(filterIndex.getAsInt() + index);
    }

    public FilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex, Supplier<HashList<? extends IFilter<?>>> filters,
          ObjIntConsumer<IFilter<?>> onPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, Component.empty(), () -> onPress.accept(getFilter(filters, filterIndex, index), filterIndex.getAsInt() + index), null);
        this.index = index;
        this.filterIndex = filterIndex;
        this.filters = filters;
        slot = addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 2, relativeY + 2));
        slotDisplay = addChild(new GuiSequencedSlotDisplay(gui, relativeX + 3, relativeY + 3,
              () -> renderStackSupplier.apply(getFilter(filters, filterIndex, index))));
        setButtonBackground(ButtonBackground.NONE);
    }

    public FilterButton warning(@NotNull WarningType type, @NotNull Predicate<IFilter<?>> hasWarning) {
        //Proxy applying the warning to the slot
        slot.warning(type, () -> hasWarning.test(getFilter(filters, filterIndex, index)));
        return this;
    }

    protected void setVisibility(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void render(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        setVisibility(getFilter(filters, filterIndex, index) != null);
        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrix, x, y, width, height, 0, isMouseOverCheckWindows(mouseX, mouseY) ? 0 : 29, TEXTURE_WIDTH, 29, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        IFilter<?> filter = getFilter(filters, filterIndex, index);
        if (filter != prevFilter) {
            slotDisplay.updateStackList();
            prevFilter = filter;
        }
        int x = this.x - getGuiLeft();
        int y = this.y - getGuiTop();
        if (filter instanceof IItemStackFilter) {
            drawFilterType(matrix, x, y, MekanismLang.ITEM_FILTER);
        } else if (filter instanceof ITagFilter) {
            drawFilterType(matrix, x, y, MekanismLang.TAG_FILTER);
        } else if (filter instanceof IMaterialFilter) {
            drawFilterType(matrix, x, y, MekanismLang.MATERIAL_FILTER);
        } else if (filter instanceof IModIDFilter) {
            drawFilterType(matrix, x, y, MekanismLang.MODID_FILTER);
        } else if (filter instanceof OredictionificatorFilter<?, ?, ?> oredictionificatorFilter) {
            drawFilterType(matrix, x, y, MekanismLang.FILTER);
            drawTextScaledBound(matrix, oredictionificatorFilter.getFilterText(), x + 22, y + 11, titleTextColor(), getMaxLength());
        }
        if (filter instanceof SorterFilter<?> sorterFilter) {
            drawTextScaledBound(matrix, sorterFilter.color == null ? MekanismLang.NONE.translate() : sorterFilter.color.getColoredName(), x + 22, y + 11,
                  titleTextColor(), getMaxLength());
        }
    }

    protected int getMaxLength() {
        return width - 22 - 2;
    }

    private void drawFilterType(PoseStack matrix, int x, int y, ILangEntry langEntry) {
        drawTextScaledBound(matrix, langEntry.translate(), x + 22, y + 2, titleTextColor(), getMaxLength());
    }
}