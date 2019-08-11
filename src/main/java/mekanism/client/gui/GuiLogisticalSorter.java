package mekanism.client.gui;

import java.io.IOException;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiColorButton;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiLogisticalSorter extends GuiFilterHolder<TileEntityLogisticalSorter, TransporterFilter> {

    private Button singleItemButton;
    private Button roundRobinButton;
    private Button autoEjectButton;
    private Button colorButton;

    public GuiLogisticalSorter(PlayerEntity player, TileEntityLogisticalSorter tile) {
        super(tile, new ContainerNull(player, tile));

        // Add common Mekanism gui elements
        ResourceLocation resource = getGuiLocation();
        addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
        addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
        addGuiElement(new GuiSecurityTab<>(this, tileEntity, resource));
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
                            sendDataFromClick(TileNetworkList.withContents(3, index));
                            return;
                        }
                        if (index < tileEntity.filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                            //Process down button click
                            sendDataFromClick(TileNetworkList.withContents(4, index));
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

        // Check for default colour button
        if (colorButton.isMouseOver(mouseX, mouseY) && mouseBtn == 1) {
            sendDataFromClick(TileNetworkList.withContents(0, 1));
        }
    }

    private void sendPacket(SorterGuiPacket type, int guiID, int extra, @Nullable SoundEvent sound) {
        Mekanism.packetHandler.sendToServer(new PacketLogisticalSorterGui(type, Coord4D.get(tileEntity), guiID, extra, 0));
        if (sound != null) {
            SoundHandler.playSound(sound);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiLogisticalSorter.png");
    }

    @Override
    public void init() {
        super.init();
        // Add buttons to gui
        buttons.clear();
        buttons.add(new Button(guiLeft + filterX, guiTop + 136, filterW, 20, LangUtils.localize("gui.newFilter"),
              onPress -> sendPacket(SorterGuiPacket.SERVER, 4, 0, null)));
        buttons.add(singleItemButton = new GuiButtonDisableableImage(guiLeft + 12, guiTop + 58, 14, 14, 204, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(5)))));
        buttons.add(roundRobinButton = new GuiButtonDisableableImage(guiLeft + 12, guiTop + 84, 14, 14, 190, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(2)))));
        buttons.add(autoEjectButton = new GuiButtonDisableableImage(guiLeft + 12, guiTop + 110, 14, 14, 176, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(1)))));
        buttons.add(colorButton = new GuiColorButton(guiLeft + 13, guiTop + 137, 16, 16, () -> tileEntity.color,
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(0, InputMappings.isKeyDown(minecraft.mainWindow.getHandle(),
                    GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0)))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Get mouse position relative to gui
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;

        // Write to info display
        font.drawString(tileEntity.getName(), 43, 6, 0x404040);
        font.drawString(LangUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
        font.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
        font.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.singleItem") + ":", 12, 48, 0x00CD00);
        font.drawString(LangUtils.transOnOff(tileEntity.singleItem), 27, 60, 0x00CD00);
        font.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.roundRobin") + ":", 12, 74, 0x00CD00);
        font.drawString(LangUtils.transOnOff(tileEntity.roundRobin), 27, 86, 0x00CD00);
        font.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.autoEject") + ":", 12, 100, 0x00CD00);
        font.drawString(LangUtils.transOnOff(tileEntity.autoEject), 27, 112, 0x00CD00);
        font.drawString(LangUtils.localize("mekanism.gui.logisticalSorter.default") + ":", 12, 126, 0x00CD00);

        // Draw filters
        for (int i = 0; i < 4; i++) {
            if (tileEntity.filters.get(getFilterIndex() + i) != null) {
                TransporterFilter filter = tileEntity.filters.get(getFilterIndex() + i);
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    IItemStackFilter itemFilter = (IItemStackFilter) filter;
                    renderItem(itemFilter.getItemStack(), 59, yStart + 3);
                    font.drawString(LangUtils.localize("gui.itemFilter"), 78, yStart + 2, 0x404040);
                    font.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IOreDictFilter) {
                    IOreDictFilter oreFilter = (IOreDictFilter) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    font.drawString(LangUtils.localize("gui.oredictFilter"), 78, yStart + 2, 0x404040);
                    font.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IMaterialFilter) {
                    IMaterialFilter itemFilter = (IMaterialFilter) filter;
                    renderItem(itemFilter.getMaterialItem(), 59, yStart + 3);
                    font.drawString(LangUtils.localize("gui.materialFilter"), 78, yStart + 2, 0x404040);
                    font.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter modFilter = (IModIDFilter) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    font.drawString(LangUtils.localize("gui.modIDFilter"), 78, yStart + 2, 0x404040);
                    font.drawString(filter.color != null ? filter.color.getColoredName() : LangUtils.localize("gui.none"), 78, yStart + 11, 0x404040);
                }

                // Draw hover text for sorting buttons
                int arrowX = filterX + filterW - 12;

                if (getFilterIndex() + i > 0 && overUpArrow(xAxis, yAxis, arrowX, yStart)) {
                    displayTooltip(LangUtils.localize("gui.moveUp"), xAxis, yAxis);
                }
                if (getFilterIndex() + i < tileEntity.filters.size() - 1 && overDownArrow(xAxis, yAxis, arrowX, yStart)) {
                    displayTooltip(LangUtils.localize("gui.moveDown"), xAxis, yAxis);
                }
            }
        }

        // Draw tooltips for buttons
        if (colorButton.isMouseOver(mouseX, mouseY)) {
            if (tileEntity.color != null) {
                displayTooltip(tileEntity.color.getColoredName(), xAxis, yAxis);
            } else {
                displayTooltip(LangUtils.localize("gui.none"), xAxis, yAxis);
            }
        } else if (autoEjectButton.isMouseOver(mouseX, mouseY)) {
            displayTooltips(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.autoEject.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        } else if (roundRobinButton.isMouseOver(mouseX, mouseY)) {
            displayTooltips(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.roundRobin.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        } else if (singleItemButton.isMouseOver(mouseX, mouseY)) {
            displayTooltips(MekanismUtils.splitTooltip(LangUtils.localize("mekanism.gui.logisticalSorter.singleItem.tooltip"), ItemStack.EMPTY), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}