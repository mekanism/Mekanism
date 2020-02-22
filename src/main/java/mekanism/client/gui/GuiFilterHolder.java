package mekanism.client.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.common.HashList;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends EmptyTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    /**
     * The number of filters that can be displayed
     */
    protected static final int FILTER_COUNT = 4;

    protected Map<ITagFilter<?>, StackData> oreDictStacks = new Object2ObjectOpenHashMap<>();
    protected Map<IModIDFilter<?>, StackData> modIDStacks = new Object2ObjectOpenHashMap<>();
    /**
     * True if the scrollbar is being dragged
     */
    protected boolean isDragging = false;
    /**
     * Amount scrolled in filter list (0 = top, 1 = bottom)
     */
    protected double scroll;
    protected int stackSwitch;
    protected int dragOffset;

    public GuiFilterHolder(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 9, 17, 46, 140));
        //Filter holder
        addButton(new GuiElementHolder(this, 55, 17, 98, 118));
        //new filter button border
        addButton(new GuiElementHolder(this, 55, 135, 98, 22));
        //Scroll bar holder
        addButton(new GuiElementHolder(this, 153, 17, 14, 140));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addButton(new FilterButton(this, 56, 18 + i * 29, i, this::getFilterIndex, this::getFilters, this::upButtonPress,
                  this::downButtonPress, this::onClick));
        }
    }

    protected HashList<FILTER> getFilters() {
        return tile.getFilters();
    }

    protected abstract void onClick(IFilter<?> filter, int index);

    protected abstract void upButtonPress(int index);

    protected abstract void downButtonPress(int index);

    public int getScroll() {
        // Calculate thumb position along scrollbar
        return Math.max(Math.min((int) (scroll * 123), 123), 0);
    }

    // Get index to displayed filters
    public int getFilterIndex() {
        if (needsScrollBars()) {
            int scrollSize = getFilters().size() - FILTER_COUNT;
            return (int) ((scrollSize + 0.5) * scroll);
        }
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        // Decrease timer for stack display rotation
        if (stackSwitch > 0) {
            stackSwitch--;
        }

        // Update displayed stacks
        if (stackSwitch == 0) {
            for (Entry<ITagFilter<?>, StackData> entry : oreDictStacks.entrySet()) {
                setNextRenderStack(entry.getValue());
            }
            for (Entry<IModIDFilter<?>, StackData> entry : modIDStacks.entrySet()) {
                setNextRenderStack(entry.getValue());
            }
            stackSwitch = 20;
        } else {
            for (Entry<ITagFilter<?>, StackData> entry : oreDictStacks.entrySet()) {
                StackData data = entry.getValue();
                if (data.iterStacks != null && data.iterStacks.isEmpty()) {
                    data.renderStack = ItemStack.EMPTY;
                }
            }
            for (Entry<IModIDFilter<?>, StackData> entry : modIDStacks.entrySet()) {
                StackData data = entry.getValue();
                if (data.iterStacks != null && data.iterStacks.isEmpty()) {
                    data.renderStack = ItemStack.EMPTY;
                }
            }
        }

        Set<ITagFilter<?>> oreDictFilters = new ObjectOpenHashSet<>();
        Set<IModIDFilter<?>> modIDFilters = new ObjectOpenHashSet<>();

        HashList<FILTER> filters = getFilters();

        for (int i = 0; i < FILTER_COUNT; i++) {
            FILTER filter = filters.get(getFilterIndex() + i);
            if (filter instanceof ITagFilter) {
                oreDictFilters.add((ITagFilter<?>) filter);
            } else if (filter instanceof IModIDFilter) {
                modIDFilters.add((IModIDFilter<?>) filter);
            }
        }

        for (FILTER filter : filters) {
            if (filter instanceof ITagFilter && !oreDictFilters.contains(filter)) {
                oreDictStacks.remove(filter);
            } else if (filter instanceof IModIDFilter && !modIDFilters.contains(filter)) {
                modIDStacks.remove(filter);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        // Draw scrollbar
        drawTexturedRect(getGuiLeft() + 154, getGuiTop() + 18 + getScroll(), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check for scrollbar interaction
            double xAxis = mouseX - getGuiLeft();
            double yAxis = mouseY - getGuiTop();
            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (needsScrollBars()) {
                    dragOffset = (int) (yAxis - (getScroll() + 18));
                    isDragging = true;
                    return true;
                } else {
                    scroll = 0;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0 && needsScrollBars()) {
            int j = getFilters().size() - FILTER_COUNT;
            if (delta > 0) {
                delta = 1;
            } else {
                delta = -1;
            }
            scroll = (float) (scroll - delta / j);
            if (scroll < 0.0F) {
                scroll = 0.0F;
            } else if (scroll > 1.0F) {
                scroll = 1.0F;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        if (isDragging) {
            // Get mouse position relative to gui
            double yAxis = mouseY - getGuiTop();
            scroll = Math.min(Math.max((yAxis - 18 - dragOffset) / 123F, 0), 1);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        if (type == 0 && isDragging) {
            dragOffset = 0;
            isDragging = false;
        }
        return true;
    }

    protected void updateStackList(ITagFilter<?> filter) {
        if (!oreDictStacks.containsKey(filter)) {
            oreDictStacks.put(filter, new StackData());
        }
        oreDictStacks.get(filter).iterStacks = getTagStacks(filter.getTagName());
        stackSwitch = 0;
        tick();
        oreDictStacks.get(filter).stackIndex = -1;
    }

    protected abstract List<ItemStack> getTagStacks(String tagName);

    protected void updateStackList(IModIDFilter<?> filter) {
        if (!modIDStacks.containsKey(filter)) {
            modIDStacks.put(filter, new StackData());
        }
        modIDStacks.get(filter).iterStacks = OreDictCache.getModIDStacks(filter.getModID(), false);
        stackSwitch = 0;
        tick();
        modIDStacks.get(filter).stackIndex = -1;
    }

    private void setNextRenderStack(StackData data) {
        if (data.iterStacks != null && !data.iterStacks.isEmpty()) {
            if (data.stackIndex == -1 || data.stackIndex == data.iterStacks.size() - 1) {
                data.stackIndex = 0;
            } else if (data.stackIndex < data.iterStacks.size() - 1) {
                data.stackIndex++;
            }
            data.renderStack = data.iterStacks.get(data.stackIndex);
        }
    }

    /**
     * returns true if there are more filters than can fit in the gui
     */
    private boolean needsScrollBars() {
        return getFilters().size() > FILTER_COUNT;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "filter_holder.png");
    }

    public static class StackData {

        public List<ItemStack> iterStacks;
        public int stackIndex;
        public ItemStack renderStack = ItemStack.EMPTY;
    }
}