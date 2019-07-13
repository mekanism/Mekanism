package mekanism.client.gui;

import java.io.IOException;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonImageMek;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismSounds;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiLogisticalSorter extends GuiFilterHolder<TileEntityLogisticalSorter, TransporterFilter> {

    private GuiButtonImageMek singleItemButton;
    private GuiButtonImageMek roundRobinButton;
    private GuiButtonImageMek autoEjectButton;

    public GuiLogisticalSorter(EntityPlayer player, TileEntityLogisticalSorter tile) {
        super(tile, new ContainerNull(player, tile));

        // Add common Mekanism gui elements
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
    }

    private boolean overColor(int xAxis, int yAxis) {
        return xAxis >= 13 && xAxis <= 29 && yAxis >= 137 && yAxis <= 153;
    }

    private boolean overUpArrow(int xAxis, int yAxis, int arrowX, int yStart) {
        return xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20;
    }

    private boolean overDownArrow(int xAxis, int yAxis, int arrowX, int yStart) {
        return xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27;
    }

    @Override
    protected HashList<TransporterFilter> getFilters() {
        return tileEntity.filters;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseBtn) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseBtn);

        // Get mouse position relative to gui
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;

        if (mouseBtn == 0) {
            // Check for scrollbar interaction
            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (needsScrollBars()) {
                    dragOffset = yAxis - (getScroll() + 18);
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }

            //Check for filter interaction
            for (int i = 0; i < 4; i++) {
                int index = getFilterIndex() + i;
                IFilter filter = tileEntity.filters.get(index);
                if (filter != null) {
                    int yStart = i * filterH + filterY;
                    if (xAxis >= filterX && xAxis <= filterX + filterW && yAxis >= yStart && yAxis <= yStart + filterH) {
                        //Check for sorting button
                        int arrowX = filterX + filterW - 12;
                        if (index > 0 && overUpArrow(xAxis, yAxis, arrowX, yStart)) {
                            //Process up button click
                            sendDataFromClick(TileNetworkList.withContents(3, index), SoundEvents.UI_BUTTON_CLICK);
                            return;
                        }
                        if (index < tileEntity.filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                            //Process down button click
                            sendDataFromClick(TileNetworkList.withContents(4, index), SoundEvents.UI_BUTTON_CLICK);
                            return;
                        }
                        if (filter instanceof IItemStackFilter) {
                            sendPacket(SorterGuiPacket.SERVER_INDEX, 1, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IOreDictFilter) {
                            sendPacket(SorterGuiPacket.SERVER_INDEX, 2, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IMaterialFilter) {
                            sendPacket(SorterGuiPacket.SERVER_INDEX, 3, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IModIDFilter) {
                            sendPacket(SorterGuiPacket.SERVER_INDEX, 5, index, SoundEvents.UI_BUTTON_CLICK);
                        }
                    }
                }
            }
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && mouseBtn == 0) {
            mouseBtn = 2;
        }

        // Check for default colour button
        if (overColor(xAxis, yAxis)) {
            sendDataFromClick(TileNetworkList.withContents(0, mouseBtn), MekanismSounds.DING);
        }
    }

    private void sendPacket(SorterGuiPacket type, int guiID, int extra, @Nullable SoundEvent sound) {
        Mekanism.packetHandler.sendToServer(new LogisticalSorterGuiMessage(type, Coord4D.get(tileEntity), guiID, extra, 0));
        if (sound != null) {
            SoundHandler.playSound(sound);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        // Add buttons to gui
        buttonList.clear();
        buttonList.add(new GuiButton(BUTTON_NEW, guiLeft + filterX, guiTop + 136, filterW, 20, LangUtils.localize("gui.newFilter")));

        singleItemButton = new GuiButtonImageMek(1, guiLeft + 12, guiTop + 58, 14, 14, 204, 14, -14, getGuiLocation());
        roundRobinButton = new GuiButtonImageMek(2, guiLeft + 12, guiTop + 84, 14, 14, 190, 14, -14, getGuiLocation());
        autoEjectButton = new GuiButtonImageMek(3, guiLeft + 12, guiTop + 110, 14, 14, 176, 14, -14, getGuiLocation());

        buttonList.add(singleItemButton);
        buttonList.add(roundRobinButton);
        buttonList.add(autoEjectButton);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == BUTTON_NEW) {
            sendPacket(SorterGuiPacket.SERVER, 4, 0, null);
        } else if (guibutton.id == singleItemButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(5)));
        } else if (guibutton.id == roundRobinButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(2)));
        } else if (guibutton.id == autoEjectButton.id) {
            Mekanism.packetHandler.sendToServer(new TileEntityMessage(tileEntity, TileNetworkList.withContents(1)));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Get mouse position relative to gui
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;

        // Write to info display
        fontRenderer.drawString(tileEntity.getName(), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
        fontRenderer.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.singleItem") + ":", 12, 48, 0x00CD00);
        fontRenderer.drawString(LangUtils.transOnOff(tileEntity.singleItem), 27, 60, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.roundRobin") + ":", 12, 74, 0x00CD00);
        fontRenderer.drawString(LangUtils.transOnOff(tileEntity.roundRobin), 27, 86, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.autoEject") + ":", 12, 100, 0x00CD00);
        fontRenderer.drawString(LangUtils.transOnOff(tileEntity.autoEject), 27, 112, 0x00CD00);
        fontRenderer.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.default") + ":", 12, 126, 0x00CD00);

        // Draw filters
        for (int i = 0; i < 4; i++) {
            if (tileEntity.filters.get(getFilterIndex() + i) != null) {
                TransporterFilter filter = tileEntity.filters.get(getFilterIndex() + i);
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    IItemStackFilter itemFilter = (IItemStackFilter) filter;
                    renderItem(itemFilter.getItemStack(), 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.itemFilter"), 78, yStart + 2, 0x404040);
                    fontRenderer.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IOreDictFilter) {
                    IOreDictFilter oreFilter = (IOreDictFilter) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.oredictFilter"), 78, yStart + 2, 0x404040);
                    fontRenderer.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IMaterialFilter) {
                    IMaterialFilter itemFilter = (IMaterialFilter) filter;
                    renderItem(itemFilter.getMaterialItem(), 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.materialFilter"), 78, yStart + 2, 0x404040);
                    fontRenderer.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter modFilter = (IModIDFilter) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.modIDFilter"), 78, yStart + 2, 0x404040);
                    fontRenderer.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                }

                // Draw hover text for sorting buttons
                int arrowX = filterX + filterW - 12;

                if (getFilterIndex() + i > 0 && overUpArrow(xAxis, yAxis, arrowX, yStart)) {
                    drawHoveringText(LangUtils.localize("gui.moveUp"), xAxis, yAxis);
                }
                if (getFilterIndex() + i < tileEntity.filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                    drawHoveringText(LangUtils.localize("gui.moveDown"), xAxis, yAxis);
                }
            }
        }
        drawColorIcon(13, 137, tileEntity.color, 1);

        // Draw tooltips for buttons
        if (overColor(xAxis, yAxis)) {
            if (tileEntity.color != null) {
                drawHoveringText(tileEntity.color.getColoredName(), xAxis, yAxis);
            } else {
                drawHoveringText(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        } else if (autoEjectButton.isMouseOver()) {
            drawHoveringText(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.autoEject.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        } else if (roundRobinButton.isMouseOver()) {
            drawHoveringText(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.roundRobin.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        } else if (singleItemButton.isMouseOver()) {
            drawHoveringText(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.singleItem.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}