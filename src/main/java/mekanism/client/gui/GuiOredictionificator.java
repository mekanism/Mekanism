package mekanism.client.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.network.PacketOredictionificatorGui;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiPacket;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.oredict.OreDictionary;

@OnlyIn(Dist.CLIENT)
public class GuiOredictionificator extends GuiMekanismTile<TileEntityOredictionificator> {

    private Map<OredictionificatorFilter, ItemStack> renderStacks = new HashMap<>();
    private boolean isDragging = false;
    private int dragOffset = 0;
    private float scroll;

    public GuiOredictionificator(PlayerInventory inventory, TileEntityOredictionificator tile) {
        super(tile, new ContainerOredictionificator(inventory, tile));
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
        addGuiElement(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tileEntity.didProcess ? 1 : 0;
            }
        }, ProgressBar.LARGE_RIGHT, this, resource, 62, 118));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 25, 114));
        addGuiElement(new GuiSlot(SlotType.NORMAL, this, resource, 133, 114));
        ySize += 64;
    }

    private boolean overFilter(int xAxis, int yAxis, int yStart) {
        return xAxis > 10 && xAxis <= 152 && yAxis > yStart && yAxis <= yStart + 22;
    }

    private int getScroll() {
        return Math.max(Math.min((int) (scroll * 73), 73), 0);
    }

    private int getFilterIndex() {
        return tileEntity.filters.size() <= 3 ? 0 : (int) (tileEntity.filters.size() * scroll - (3F / (float) tileEntity.filters.size()) * scroll);
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(new Button(guiLeft + 10, guiTop + 86, 142, 20, LangUtils.localize("gui.newFilter"),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketOredictionificatorGui(OredictionificatorGuiPacket.SERVER, Coord4D.get(tileEntity), 1, 0, 0))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        font.drawString(tileEntity.getName(), (xSize / 2) - (font.getStringWidth(tileEntity.getName()) / 2), 6, 0x404040);
        font.drawString(LangUtils.localize("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
        for (int i = 0; i < 3; i++) {
            if (tileEntity.filters.get(getFilterIndex() + i) != null) {
                OredictionificatorFilter filter = tileEntity.filters.get(getFilterIndex() + i);
                if (!renderStacks.containsKey(filter)) {
                    updateRenderStacks();
                }
                int yStart = i * 22 + 18;
                renderItem(renderStacks.get(filter), 13, yStart + 3);
                font.drawString(LangUtils.localize("gui.filter"), 32, yStart + 2, 0x404040);
                renderScaledText(filter.filter, 32, yStart + 2 + 9, 0x404040, 117);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 154, guiTop + 18 + getScroll(), 232, 0, 12, 15);
        for (int i = 0; i < 3; i++) {
            if (tileEntity.filters.get(getFilterIndex() + i) != null) {
                int yStart = i * 22 + 18;
                boolean mouseOver = overFilter(xAxis, yAxis, yStart);
                if (mouseOver) {
                    MekanismRenderer.color(EnumColor.GREY);
                }
                drawTexturedRect(guiLeft + 10, guiTop + yStart, 0, 230, 142, 22);
                if (mouseOver) {
                    MekanismRenderer.resetColor();
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;
            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (tileEntity.filters.size() > 3) {
                    dragOffset = yAxis - (getScroll() + 18);
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }

            for (int i = 0; i < 3; i++) {
                if (tileEntity.filters.get(getFilterIndex() + i) != null && overFilter(xAxis, yAxis, i * 22 + 18)) {
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                    Mekanism.packetHandler.sendToServer(new PacketOredictionificatorGui(OredictionificatorGuiPacket.SERVER_INDEX, Coord4D.get(tileEntity), 1, getFilterIndex() + i, 0));
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
        super.mouseClickMove(mouseX, mouseY, button, ticks);
        if (isDragging) {
            int yAxis = mouseY - (height - ySize) / 2;
            scroll = Math.min(Math.max((float) (yAxis - 18 - dragOffset) / 73F, 0), 1);
        }
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
        return MekanismUtils.getResource(ResourceType.GUI, "GuiOredictionificator.png");
    }

    public void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : tileEntity.filters) {
            if (filter.filter == null || filter.filter.isEmpty()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            List<ItemStack> stacks = OreDictionary.getOres(filter.filter, false);
            if (stacks.isEmpty()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            if (stacks.size() - 1 >= filter.index) {
                renderStacks.put(filter, stacks.get(filter.index).copy());
            } else {
                renderStacks.put(filter, ItemStack.EMPTY);
            }
        }
    }
}