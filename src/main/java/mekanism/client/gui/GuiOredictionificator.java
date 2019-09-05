package mekanism.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.OredictionificatorContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiOredictionificator extends GuiMekanismTile<TileEntityOredictionificator, OredictionificatorContainer> {

    private Map<OredictionificatorFilter, ItemStack> renderStacks = new HashMap<>();
    private boolean isDragging = false;
    private double dragOffset = 0;
    private double scroll;

    public GuiOredictionificator(OredictionificatorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiRedstoneControl(this, tileEntity, resource));
        addButton(new GuiSecurityTab<>(this, tileEntity, resource));
        addButton(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.didProcess ? 1 : 0;
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 118));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 25, 114));
        addButton(new GuiSlot(SlotType.NORMAL, this, resource, 133, 114));

        addButton(new TranslationButton(this, guiLeft + 10, guiTop + 86, 142, 20, "gui.mekanism.newFilter",
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tileEntity.getPos(), 0))));
    }

    private boolean overFilter(double xAxis, double yAxis, int yStart) {
        return xAxis > 10 && xAxis <= 152 && yAxis > yStart && yAxis <= yStart + 22;
    }

    private int getScroll() {
        return Math.max(Math.min((int) (scroll * 73), 73), 0);
    }

    private int getFilterIndex() {
        int size = tileEntity.getFilters().size();
        return size <= 3 ? 0 : (int) (size * scroll - (3F / (float) size) * scroll);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tileEntity.getName(), (xSize / 2) - (getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        drawString(TextComponentUtil.translate("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        HashList<OredictionificatorFilter> filters = tileEntity.getFilters();
        for (int i = 0; i < 3; i++) {
            if (filters.get(getFilterIndex() + i) != null) {
                OredictionificatorFilter filter = filters.get(getFilterIndex() + i);
                if (!renderStacks.containsKey(filter)) {
                    updateRenderStacks();
                }
                int yStart = i * 22 + 18;
                renderItem(renderStacks.get(filter), 13, yStart + 3);
                drawString(TextComponentUtil.translate("gui.mekanism.filter"), 32, yStart + 2, 0x404040);
                renderScaledText(filter.getFilterText(), 32, yStart + 2 + 9, 0x404040, 117);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 154, guiTop + 18 + getScroll(), 232, 0, 12, 15);
        HashList<OredictionificatorFilter> filters = tileEntity.getFilters();
        for (int i = 0; i < 3; i++) {
            if (filters.get(getFilterIndex() + i) != null) {
                int yStart = i * 22 + 18;
                boolean mouseOver = overFilter(xAxis, yAxis, yStart);
                if (mouseOver) {
                    MekanismRenderer.color(EnumColor.GRAY);
                }
                drawTexturedRect(guiLeft + 10, guiTop + yStart, 0, 230, 142, 22);
                if (mouseOver) {
                    MekanismRenderer.resetColor();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;
            HashList<OredictionificatorFilter> filters = tileEntity.getFilters();
            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (filters.size() > 3) {
                    dragOffset = yAxis - (getScroll() + 18);
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }

            for (int i = 0; i < 3; i++) {
                if (filters.get(getFilterIndex() + i) != null && overFilter(xAxis, yAxis, i * 22 + 18)) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tileEntity.getPos(), getFilterIndex() + i));
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double mouseXOld, double mouseYOld) {
        //TODO: mouseXOld and mouseYOld are just guessed mappings I couldn't find any usage from a quick glance. look closer
        super.mouseDragged(mouseX, mouseY, button, mouseXOld, mouseYOld);
        if (isDragging) {
            double yAxis = mouseY - (height - ySize) / 2D;
            scroll = Math.min(Math.max((yAxis - 18 - dragOffset) / 73F, 0), 1);
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

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "oredictionificator.png");
    }

    public void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : tileEntity.getFilters()) {
            if (filter.hasFilter()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            List<Item> matchingItems = filter.getMatchingItems();
            if (matchingItems.isEmpty()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            if (matchingItems.size() - 1 >= filter.index) {
                renderStacks.put(filter, new ItemStack(matchingItems.get(filter.index)));
            } else {
                renderStacks.put(filter, ItemStack.EMPTY);
            }
        }
    }
}