package mekanism.client.gui.element.button;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterButton extends MekanismButton {

    private static final ResourceLocation TEXTURE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_holder.png");
    protected static final int TEXTURE_WIDTH = 156;
    protected static final int TEXTURE_HEIGHT = 58;

    protected final FilterManager<?> filterManager;
    private final GuiSequencedSlotDisplay slotDisplay;
    private final ObjIntConsumer<IFilter<?>> onPress;
    private final IntSupplier filterIndex;
    private final RadioButton toggleButton;
    private final GuiSlot slot;
    private final int index;
    private IFilter<?> prevFilter;

    @Nullable
    private static IFilter<?> getFilter(FilterManager<?> filterManager, int index) {
        if (index >= 0 && index < filterManager.count()) {
            return filterManager.getFilters().get(index);
        }
        return null;
    }

    public FilterButton(IGuiWrapper gui, int x, int y, int width, int height, int index, IntSupplier filterIndex, FilterManager<?> filterManager,
          ObjIntConsumer<IFilter<?>> onPress, IntConsumer toggleButtonPress, Function<IFilter<?>, List<ItemStack>> renderStackSupplier) {
        super(gui, x, y, width, height, CommonComponents.EMPTY, (element, mouseX, mouseY) -> {
            FilterButton button = (FilterButton) element;
            int actualIndex = button.filterIndex.getAsInt() + button.index;
            button.onPress.accept(getFilter(button.filterManager, actualIndex), actualIndex);
            return true;
        });
        this.index = index;
        this.filterIndex = filterIndex;
        this.filterManager = filterManager;
        this.onPress = onPress;
        slot = addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 2, relativeY + 2));
        slotDisplay = addChild(new GuiSequencedSlotDisplay(gui, relativeX + 3, relativeY + 3, () -> renderStackSupplier.apply(getFilter())));
        toggleButton = addChild(new RadioButton(gui, relativeX + this.width - RadioButton.RADIO_SIZE - getToggleXShift(), relativeY + (this.height / 2) - (RadioButton.RADIO_SIZE / 2),
              this::isEnabled, (element, mouseX, mouseY) -> {
            toggleButtonPress.accept(getActualIndex());
            return true;
        }, MekanismLang.FILTER_STATE.translate(EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER), MekanismLang.FILTER_STATE.translate(EnumColor.RED, MekanismLang.MODULE_DISABLED_LOWER)));
        setButtonBackground(ButtonBackground.NONE);
    }

    private boolean isEnabled() {
        IFilter<?> filter = getFilter();
        return filter != null && filter.isEnabled();
    }

    protected int getToggleXShift() {
        return 4;
    }

    protected int getActualIndex() {
        return filterIndex.getAsInt() + index;
    }

    @Nullable
    protected IFilter<?> getFilter() {
        return getFilter(filterManager, getActualIndex());
    }

    public FilterButton warning(@NotNull WarningType type, @NotNull Predicate<IFilter<?>> hasWarning) {
        //Proxy applying the warning to the slot
        slot.warning(type, () -> hasWarning.test(getFilter()));
        return this;
    }

    protected void setVisibility(boolean visible) {
        //TODO: Should we check visibility before passing things like tooltip to children? That way we don't have to manually hide the children as well
        this.visible = visible;
        this.slot.visible = visible;
        this.slotDisplay.visible = visible;
        this.toggleButton.visible = visible;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        setVisibility(getFilter() != null);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(TEXTURE, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 0, isMouseOverCheckWindows(mouseX, mouseY) ? 0 : 29, TEXTURE_WIDTH, 29, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        IFilter<?> filter = getFilter();
        if (filter != prevFilter) {
            slotDisplay.updateStackList();
            prevFilter = filter;
        }
        Component filterDescriptor = switch (filter) {
            case IItemStackFilter<?> item -> item.getItemStack().getHoverName();
            case ITagFilter<?> tag -> TextComponentUtil.getString(tag.getTagName());
            case IModIDFilter<?> modId -> TextComponentUtil.getString(modId.getModID());
            case OredictionificatorFilter<?, ?, ?> oredictionificatorFilter -> TextComponentUtil.getString(oredictionificatorFilter.getFilterText());
            case null, default -> null;
        };
        int textWidth = toggleButton.getRelativeX() - relativeX - 20;
        if (filterDescriptor != null) {
            drawScrollingString(guiGraphics, filterDescriptor, 19, 3, TextAlignment.LEFT, titleTextColor(), textWidth, 3, false);
        }

        if (filter instanceof SorterFilter<?> sorterFilter) {
            int colorX = relativeX + 22;
            int colorY = relativeY + 13;
            GuiUtils.drawOutline(guiGraphics, colorX, colorY, 6, 6, 0xFF393939);
            if (sorterFilter.color != null) {
                guiGraphics.fill(colorX + 1, colorY + 1, colorX + 5, colorY + 5, sorterFilter.color.getPackedColor());
            }

            Component color = sorterFilter.color == null ? MekanismLang.NO_COLOR.translate() : sorterFilter.color.getName();
            drawScaledScrollingString(guiGraphics, color, 27, 12, TextAlignment.LEFT, titleTextColor(), textWidth - 8, 3, false, 0.7F);
        } else if (filter instanceof OredictionificatorItemFilter oreDictFilter) {
            ItemStack result = oreDictFilter.getResult();
            Component text = TextComponentUtil.build(result, " (", MekanismUtils.getModId(result), ")");
            drawScaledScrollingString(guiGraphics, text, 19, 12, TextAlignment.LEFT, titleTextColor(), textWidth, 3, false, 0.7F);
        } else if (filter instanceof QIOItemStackFilter itemFilter) {
            if (itemFilter.fuzzyMode) {
                drawScaledScrollingString(guiGraphics, MekanismLang.FUZZY_MODE.translate(), 19, 12, TextAlignment.LEFT, titleTextColor(), textWidth, 3, false, 0.7F);
            }
        }
    }
}