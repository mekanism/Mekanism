package mekanism.client.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.HashList;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends EmptyTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 4;

    private Map<ITagFilter<?>, StackData> oreDictStacks = new Object2ObjectOpenHashMap<>();
    private Map<IModIDFilter<?>, StackData> modIDStacks = new Object2ObjectOpenHashMap<>();
    private GuiScrollBar scrollBar;
    private int stackSwitch;

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
        addButton(scrollBar = new GuiScrollBar(this, 153, 17, 140, () -> getFilters().size(), () -> FILTER_COUNT));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addButton(new MovableFilterButton(this, 56, 18 + i * 29, i, scrollBar::getCurrentSelection, this::getFilters, this::upButtonPress,
                  this::downButtonPress, this::onClick));
        }
    }

    protected HashList<FILTER> getFilters() {
        return tile.getFilters();
    }

    protected abstract void onClick(IFilter<?> filter, int index);

    protected abstract void upButtonPress(int index);

    protected abstract void downButtonPress(int index);

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
            FILTER filter = filters.get(scrollBar.getCurrentSelection() + i);
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //TODO: Eventually we may want to move the item drawing into FilterButton, but for now it does not matter
        HashList<? extends IFilter<?>> filters = getFilters();
        for (int i = 0; i < FILTER_COUNT; i++) {
            IFilter<?> filter = filters.get(scrollBar.getCurrentSelection() + i);
            if (filter != null) {
                int yStart = i * 29 + 18;
                if (filter instanceof IItemStackFilter) {
                    renderItem(((IItemStackFilter<?>) filter).getItemStack(), 59, yStart + 3);
                } else if (filter instanceof ITagFilter) {
                    ITagFilter<?> oreFilter = (ITagFilter<?>) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                } else if (filter instanceof IMaterialFilter) {
                    renderItem(((IMaterialFilter<?>) filter).getMaterialItem(), 59, yStart + 3);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter<?> modFilter = (IModIDFilter<?>) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateStackList(ITagFilter<?> filter) {
        if (!oreDictStacks.containsKey(filter)) {
            oreDictStacks.put(filter, new StackData());
        }
        oreDictStacks.get(filter).iterStacks = getTagStacks(filter.getTagName());
        stackSwitch = 0;
        tick();
        oreDictStacks.get(filter).stackIndex = -1;
    }

    protected abstract List<ItemStack> getTagStacks(String tagName);

    private void updateStackList(IModIDFilter<?> filter) {
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

    public static class StackData {

        public List<ItemStack> iterStacks;
        public int stackIndex;
        public ItemStack renderStack = ItemStack.EMPTY;
    }
}