package mekanism.client.gui;

import java.io.IOException;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiDigitalMinerConfig extends GuiFilterHolder<TileEntityDigitalMiner, MinerFilter> {

    private GuiTextField radiusField;
    private GuiTextField minField;
    private GuiTextField maxField;

    public GuiDigitalMinerConfig(EntityPlayer player, TileEntityDigitalMiner tile) {
        super(tile, new ContainerNull(player, tile));
    }

    @Override
    protected HashList<MinerFilter> getFilters() {
        return tileEntity.filters;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        radiusField.updateCursorCounter();
        minField.updateCursorCounter();
        maxField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);

        radiusField.mouseClicked(mouseX, mouseY, button);
        minField.mouseClicked(mouseX, mouseY, button);
        maxField.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            int xAxis = mouseX - guiLeft;
            int yAxis = mouseY - guiTop;

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
                        if (index > 0) {
                            if (xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 14 && yAxis <= yStart + 20) {
                                //Process up button click
                                sendDataFromClick(TileNetworkList.withContents(11, index));
                                return;
                            }
                        }
                        if (index < tileEntity.filters.size() - 1) {
                            if (xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27) {
                                //Process down button click
                                sendDataFromClick(TileNetworkList.withContents(12, index));
                                return;
                            }
                        }
                        if (filter instanceof IItemStackFilter) {
                            sendPacket(MinerGuiPacket.SERVER_INDEX, 1, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IOreDictFilter) {
                            sendPacket(MinerGuiPacket.SERVER_INDEX, 2, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IMaterialFilter) {
                            sendPacket(MinerGuiPacket.SERVER_INDEX, 3, index, SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IModIDFilter) {
                            sendPacket(MinerGuiPacket.SERVER_INDEX, 6, index, SoundEvents.UI_BUTTON_CLICK);
                        }
                    }
                }
            }

            if (xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16) {
                sendPacket(MinerGuiPacket.SERVER, 4, 0, SoundEvents.UI_BUTTON_CLICK);
            } else if (xAxis >= 39 && xAxis <= 50 && yAxis >= 67 && yAxis <= 78) {
                setRadius();
            } else if (xAxis >= 39 && xAxis <= 50 && yAxis >= 92 && yAxis <= 103) {
                setMinY();
            } else if (xAxis >= 39 && xAxis <= 50 && yAxis >= 117 && yAxis <= 128) {
                setMaxY();
            } else if (xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155) {
                sendDataFromClick(TileNetworkList.withContents(10));
            }
        }
    }

    private void sendPacket(MinerGuiPacket type, int guiID, int extra, @Nullable SoundEvent sound) {
        Mekanism.packetHandler.sendToServer(new DigitalMinerGuiMessage(type, Coord4D.get(tileEntity), guiID, extra, 0));
        if (sound != null) {
            SoundHandler.playSound(sound);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMinerConfig.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(new GuiButton(0, guiLeft + filterX, guiTop + 136, filterW, 20, LangUtils.localize("gui.newFilter")));

        String prevRad = radiusField != null ? radiusField.getText() : "";
        String prevMin = minField != null ? minField.getText() : "";
        String prevMax = maxField != null ? maxField.getText() : "";

        radiusField = new GuiTextField(1, fontRenderer, guiLeft + 12, guiTop + 67, 26, 11);
        radiusField.setMaxStringLength(Integer.toString(MekanismConfig.current().general.digitalMinerMaxRadius.val()).length());
        radiusField.setText(prevRad);

        minField = new GuiTextField(2, fontRenderer, guiLeft + 12, guiTop + 92, 26, 11);
        minField.setMaxStringLength(3);
        minField.setText(prevMin);

        maxField = new GuiTextField(3, fontRenderer, guiLeft + 12, guiTop + 117, 26, 11);
        maxField.setMaxStringLength(3);
        maxField.setText(prevMax);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == BUTTON_NEW) {
            sendPacket(MinerGuiPacket.SERVER, 5, 0, null);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(LangUtils.localize("gui.digitalMinerConfig"), 43, 6, 0x404040);
        fontRenderer.drawString(LangUtils.localize("gui.filters") + ":", 11, 19, 0x00CD00);
        fontRenderer.drawString("T: " + tileEntity.filters.size(), 11, 28, 0x00CD00);
        fontRenderer.drawString("I: " + (tileEntity.inverse ? LangUtils.localize("gui.on") : LangUtils.localize("gui.off")), 11, 131, 0x00CD00);
        fontRenderer.drawString("Radi: " + tileEntity.getRadius(), 11, 58, 0x00CD00);
        fontRenderer.drawString("Min: " + tileEntity.minY, 11, 83, 0x00CD00);
        fontRenderer.drawString("Max: " + tileEntity.maxY, 11, 108, 0x00CD00);

        for (int i = 0; i < 4; i++) {
            IFilter filter = tileEntity.filters.get(getFilterIndex() + i);
            if (filter != null) {
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    renderItem(((IItemStackFilter) filter).getItemStack(), 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.itemFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IOreDictFilter) {
                    IOreDictFilter oreFilter = (IOreDictFilter) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.oredictFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IMaterialFilter) {
                    renderItem(((IMaterialFilter) filter).getMaterialItem(), 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.materialFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter modFilter = (IModIDFilter) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    fontRenderer.drawString(LangUtils.localize("gui.modIDFilter"), 78, yStart + 2, 0x404040);
                }
            }
        }
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155) {
            drawHoveringText(LangUtils.localize("gui.digitalMiner.inverse"), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedModalRect(guiLeft + 5, guiTop + 5, 176, 0, xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16, 11);
        drawTexturedModalRect(guiLeft + 39, guiTop + 67, 187, 0, xAxis >= 39 && xAxis <= 50 && yAxis >= 67 && yAxis <= 78, 11);
        drawTexturedModalRect(guiLeft + 39, guiTop + 92, 187, 0, xAxis >= 39 && xAxis <= 50 && yAxis >= 92 && yAxis <= 103, 11);
        drawTexturedModalRect(guiLeft + 39, guiTop + 117, 187, 0, xAxis >= 39 && xAxis <= 50 && yAxis >= 117 && yAxis <= 128, 11);
        drawTexturedModalRect(guiLeft + 11, guiTop + 141, 198, 0, xAxis >= 11 && xAxis <= 25 && yAxis >= 141 && yAxis <= 155, 14);

        radiusField.drawTextBox();
        minField.drawTextBox();
        maxField.drawTextBox();
    }

    @Override
    public void keyTyped(char c, int i) throws IOException {
        if ((!radiusField.isFocused() && !minField.isFocused() && !maxField.isFocused()) || i == Keyboard.KEY_ESCAPE) {
            super.keyTyped(c, i);
        }
        if (i == Keyboard.KEY_RETURN) {
            if (radiusField.isFocused()) {
                setRadius();
            } else if (minField.isFocused()) {
                setMinY();
            } else if (maxField.isFocused()) {
                setMaxY();
            }
        }
        if (Character.isDigit(c) || isTextboxKey(c, i)) {
            radiusField.textboxKeyTyped(c, i);
            minField.textboxKeyTyped(c, i);
            maxField.textboxKeyTyped(c, i);
        }
    }

    private void setRadius() {
        if (!radiusField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(radiusField.getText()), MekanismConfig.current().general.digitalMinerMaxRadius.val()));
            sendDataFromClick(TileNetworkList.withContents(6, toUse));
            radiusField.setText("");
        }
    }

    private void setMinY() {
        if (!minField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(minField.getText()), tileEntity.maxY));
            sendDataFromClick(TileNetworkList.withContents(7, toUse));
            minField.setText("");
        }
    }

    private void setMaxY() {
        if (!maxField.getText().isEmpty()) {
            int toUse = Math.max(tileEntity.minY, Math.min(Integer.parseInt(maxField.getText()), 255));
            sendDataFromClick(TileNetworkList.withContents(8, toUse));
            maxField.setText("");
        }
    }
}