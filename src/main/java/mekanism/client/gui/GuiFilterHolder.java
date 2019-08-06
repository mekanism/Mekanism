package mekanism.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.input.Mouse;

@OnlyIn(Dist.CLIENT)
public abstract class GuiFilterHolder<TILE extends TileEntityMekanism, FILTER extends IFilter> extends GuiMekanismTile<TILE> {

    // Filter dimensions
    protected final int filterX = 56;
    protected final int filterY = 18;
    protected final int filterW = 96;
    protected final int filterH = 29;

    protected Map<IOreDictFilter, StackData> oreDictStacks = new HashMap<>();
    protected Map<IModIDFilter, StackData> modIDStacks = new HashMap<>();
    /**
     * True if the scrollbar is being dragged
     */
    protected boolean isDragging = false;
    /**
     * Amount scrolled in filter list (0 = top, 1 = bottom)
     */
    protected float scroll;
    protected int stackSwitch;
    protected int dragOffset;
    // Buttons
    protected final int BUTTON_NEW = 0;

    public GuiFilterHolder(TILE tile, Container container) {
        super(tile, container);
    }

    protected abstract HashList<FILTER> getFilters();

    public int getScroll() {
        // Calculate thumb position along scrollbar
        return Math.max(Math.min((int) (scroll * 123), 123), 0);
    }

    // Get index to displayed filters
    public int getFilterIndex() {
        if (needsScrollBars()) {
            int scrollSize = getFilters().size() - 4;
            return (int) ((scrollSize + 0.5) * scroll);
        }
        return 0;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Decrease timer for stack display rotation
        if (stackSwitch > 0) {
            stackSwitch--;
        }

        // Update displayed stacks
        if (stackSwitch == 0) {
            for (Entry<IOreDictFilter, StackData> entry : oreDictStacks.entrySet()) {
                setNextRenderStack(entry.getValue());
            }
            for (Entry<IModIDFilter, StackData> entry : modIDStacks.entrySet()) {
                setNextRenderStack(entry.getValue());
            }
            stackSwitch = 20;
        } else {
            for (Entry<IOreDictFilter, StackData> entry : oreDictStacks.entrySet()) {
                StackData data = entry.getValue();
                if (data.iterStacks != null && data.iterStacks.size() == 0) {
                    data.renderStack = ItemStack.EMPTY;
                }
            }
            for (Entry<IModIDFilter, StackData> entry : modIDStacks.entrySet()) {
                StackData data = entry.getValue();
                if (data.iterStacks != null && data.iterStacks.size() == 0) {
                    data.renderStack = ItemStack.EMPTY;
                }
            }
        }

        Set<IOreDictFilter> oreDictFilters = new HashSet<>();
        Set<IModIDFilter> modIDFilters = new HashSet<>();

        HashList<FILTER> filters = getFilters();

        for (int i = 0; i < 4; i++) {
            FILTER filter = filters.get(getFilterIndex() + i);
            if (filter instanceof IOreDictFilter) {
                oreDictFilters.add((IOreDictFilter) filter);
            } else if (filter instanceof IModIDFilter) {
                modIDFilters.add((IModIDFilter) filter);
            }
        }

        for (IFilter filter : filters) {
            if (filter instanceof IOreDictFilter && !oreDictFilters.contains(filter)) {
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
        drawTexturedModalRect(guiLeft + 154, guiTop + 18 + getScroll(), 232 + (needsScrollBars() ? 0 : 12), 0, 12, 15);

        HashList<FILTER> filters = getFilters();
        // Draw filter backgrounds
        for (int i = 0; i < 4; i++) {
            IFilter filter = filters.get(getFilterIndex() + i);
            if (filter != null) {
                // Change colour based on filter type
                if (filter instanceof IItemStackFilter) {
                    MekanismRenderer.color(EnumColor.INDIGO, 1.0F, 2.5F);
                } else if (filter instanceof IOreDictFilter) {
                    MekanismRenderer.color(EnumColor.BRIGHT_GREEN, 1.0F, 2.5F);
                } else if (filter instanceof IMaterialFilter) {
                    MekanismRenderer.color(EnumColor.PURPLE, 1.0F, 4F);
                } else if (filter instanceof IModIDFilter) {
                    MekanismRenderer.color(EnumColor.PINK, 1.0F, 2.5F);
                }
                int yStart = i * filterH + filterY;
                // Flag for mouse over this filter
                boolean mouseOver = xAxis >= filterX && xAxis <= filterX + filterW && yAxis >= yStart && yAxis <= yStart + filterH;
                drawTexturedModalRect(guiLeft + filterX, guiTop + yStart, mouseOver ? 0 : filterW, 166, filterW, filterH);
                MekanismRenderer.resetColor();

                // Draw sort buttons
                int arrowX = filterX + filterW - 12;
                if (getFilterIndex() + i > 0) {
                    mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
                    drawTexturedModalRect(guiLeft + arrowX, guiTop + yStart + 14, 190, mouseOver ? 143 : 115, 11, 7);
                }
                if (getFilterIndex() + i < filters.size() - 1) {
                    mouseOver = xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
                    drawTexturedModalRect(guiLeft + arrowX, guiTop + yStart + 21, 190, mouseOver ? 157 : 129, 11, 7);
                }
            }
        }

    }

    /**
     * Handles mouse input.
     */
    @Override
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
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
        super.mouseClickMove(mouseX, mouseY, button, ticks);
        if (isDragging) {
            // Get mouse position relative to gui
            int yAxis = mouseY - guiTop;
            scroll = Math.min(Math.max((yAxis - 18 - dragOffset) / 123F, 0), 1);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int type) {
        super.mouseReleased(mouseX, mouseY, type);
        if (type == 0 && isDragging) {
            dragOffset = 0;
            isDragging = false;
        }
    }

    protected void updateStackList(IOreDictFilter filter) {
        if (!oreDictStacks.containsKey(filter)) {
            oreDictStacks.put(filter, new StackData());
        }
        oreDictStacks.get(filter).iterStacks = OreDictCache.getOreDictStacks(filter.getOreDictName(), false);
        stackSwitch = 0;
        updateScreen();
        oreDictStacks.get(filter).stackIndex = -1;
    }

    protected void updateStackList(IModIDFilter filter) {
        if (!modIDStacks.containsKey(filter)) {
            modIDStacks.put(filter, new StackData());
        }
        modIDStacks.get(filter).iterStacks = OreDictCache.getModIDStacks(filter.getModID(), false);
        stackSwitch = 0;
        updateScreen();
        modIDStacks.get(filter).stackIndex = -1;
    }

    protected void sendDataFromClick(TileNetworkList data) {
        Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, data));
        SoundHandler.playSound(net.minecraft.init.SoundEvents.UI_BUTTON_CLICK);
    }

    private void setNextRenderStack(StackData data) {
        if (data.iterStacks != null && data.iterStacks.size() > 0) {
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
        return getFilters().size() > 4;
    }

    public static class StackData {

        public List<ItemStack> iterStacks;
        public int stackIndex;
        public ItemStack renderStack = ItemStack.EMPTY;
    }
}