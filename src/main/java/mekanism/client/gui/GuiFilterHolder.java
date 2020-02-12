package mekanism.client.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.TileNetworkList;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends EmptyTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    // Filter dimensions
    protected final int filterX = 56;
    protected final int filterY = 18;
    protected final int filterW = 96;
    protected final int filterH = 29;

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

    public int getScroll() {
        // Calculate thumb position along scrollbar
        return Math.max(Math.min((int) (scroll * 123), 123), 0);
    }

    // Get index to displayed filters
    public int getFilterIndex() {
        if (needsScrollBars()) {
            int scrollSize = tile.getFilters().size() - 4;
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

        HashList<FILTER> filters = tile.getFilters();

        for (int i = 0; i < 4; i++) {
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

        HashList<FILTER> filters = tile.getFilters();
        // Draw filter backgrounds
        for (int i = 0; i < 4; i++) {
            FILTER filter = filters.get(getFilterIndex() + i);
            if (filter != null) {
                // Change color based on filter type
                if (filter instanceof IItemStackFilter) {
                    MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
                } else if (filter instanceof ITagFilter) {
                    MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
                } else if (filter instanceof IMaterialFilter) {
                    MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
                } else if (filter instanceof IModIDFilter) {
                    MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
                }
                int yStart = i * filterH + filterY;
                // Flag for mouse over this filter
                boolean mouseOver = xAxis >= filterX && xAxis <= filterX + filterW && yAxis >= yStart && yAxis <= yStart + filterH;
                drawTexturedRect(getGuiLeft() + filterX, getGuiTop() + yStart, mouseOver ? 0 : filterW, 166, filterW, filterH);
                MekanismRenderer.resetColor();

                // Draw sort buttons
                int arrowX = filterX + filterW - 12;
                if (getFilterIndex() + i > 0) {
                    mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
                    drawTexturedRect(getGuiLeft() + arrowX, getGuiTop() + yStart + 14, 190, mouseOver ? 143 : 115, 11, 7);
                }
                if (getFilterIndex() + i < filters.size() - 1) {
                    mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
                    drawTexturedRect(getGuiLeft() + arrowX, getGuiTop() + yStart + 21, 190, mouseOver ? 157 : 129, 11, 7);
                }
            }
        }

    }

    /**
     * Handles mouse input.
     */
    //TODO: Mouse scrolling
    /*@Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0 && needsScrollBars()) {
            int j = getFilters().size() - 4;
            if (i > 0) {
                i = 1;
            } else {
                i = -1;
            }
            scroll = (float) (scroll - (double) i / (double) j);
            if (scroll < 0.0F) {
                scroll = 0.0F;
            } else if (scroll > 1.0F) {
                scroll = 1.0F;
            }
        }
    }*/
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
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

    protected void sendDataFromClick(TileNetworkList data) {
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, data));
        SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
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
    protected boolean needsScrollBars() {
        return tile.getFilters().size() > 4;
    }

    public static class StackData {

        public List<ItemStack> iterStacks;
        public int stackIndex;
        public ItemStack renderStack = ItemStack.EMPTY;
    }
}