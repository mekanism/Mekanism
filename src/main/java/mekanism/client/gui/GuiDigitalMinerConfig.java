package mekanism.client.gui;

import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.GuiButtonDisableableImage;
import mekanism.client.gui.button.GuiButtonTranslation;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.config_old.MekanismConfigOld;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.MinerGuiPacket;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiDigitalMinerConfig extends GuiFilterHolder<TileEntityDigitalMiner, MinerFilter> {

    private TextFieldWidget radiusField;
    private TextFieldWidget minField;
    private TextFieldWidget maxField;
    private Button newFilterButton;
    private Button backButton;
    private Button setRadiButton;
    private Button setMinButton;
    private Button setMaxButton;
    private Button inverseButton;

    public GuiDigitalMinerConfig(PlayerEntity player, TileEntityDigitalMiner tile) {
        super(tile, new ContainerNull(player, tile));
    }

    @Override
    protected HashList<MinerFilter> getFilters() {
        return tileEntity.filters;
    }

    @Override
    public void tick() {
        super.tick();
        radiusField.tick();
        minField.tick();
        maxField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        radiusField.mouseClicked(mouseX, mouseY, button);
        minField.mouseClicked(mouseX, mouseY, button);
        maxField.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;

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
                                return true;
                            }
                        }
                        if (index < tileEntity.filters.size() - 1) {
                            if (xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27) {
                                //Process down button click
                                sendDataFromClick(TileNetworkList.withContents(12, index));
                                return true;
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
        }
    }

    private void sendPacket(MinerGuiPacket type, int guiID, int extra, @Nullable SoundEvent sound) {
        Mekanism.packetHandler.sendToServer(new PacketDigitalMinerGui(type, Coord4D.get(tileEntity), guiID, extra, 0));
        if (sound != null) {
            SoundHandler.playSound(sound);
        }
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "GuiDigitalMinerConfig.png");
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        buttons.add(newFilterButton = new GuiButtonTranslation(guiLeft + filterX, guiTop + 136, filterW, 20, "gui.newFilter",
              onPress -> sendPacket(MinerGuiPacket.SERVER, 5, 0, null)));
        buttons.add(backButton = new GuiButtonDisableableImage(guiLeft + 5, guiTop + 5, 11, 11, 176, 11, -11, getGuiLocation(),
              onPress -> sendPacket(MinerGuiPacket.SERVER, 4, 0, null)));
        buttons.add(setRadiButton = new GuiButtonDisableableImage(guiLeft + 39, guiTop + 67, 11, 11, 187, 11, -11, getGuiLocation(),
              onPress -> setRadius()));
        buttons.add(setMinButton = new GuiButtonDisableableImage(guiLeft + 39, guiTop + 92, 11, 11, 187, 11, -11, getGuiLocation(),
              onPress -> setMinY()));
        buttons.add(setMaxButton = new GuiButtonDisableableImage(guiLeft + 39, guiTop + 117, 11, 11, 187, 11, -11, getGuiLocation(),
              onPress -> setMaxY()));
        buttons.add(inverseButton = new GuiButtonDisableableImage(guiLeft + 11, guiTop + 141, 14, 14, 198, 14, -14, getGuiLocation(),
              onPress -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(10)))));

        String prevRad = radiusField != null ? radiusField.getText() : "";
        String prevMin = minField != null ? minField.getText() : "";
        String prevMax = maxField != null ? maxField.getText() : "";

        radiusField = new TextFieldWidget(font, guiLeft + 12, guiTop + 67, 26, 11, "");
        radiusField.setMaxStringLength(Integer.toString(MekanismConfigOld.current().general.digitalMinerMaxRadius.get()).length());
        radiusField.setText(prevRad);

        minField = new TextFieldWidget(font, guiLeft + 12, guiTop + 92, 26, 11, "");
        minField.setMaxStringLength(3);
        minField.setText(prevMin);

        maxField = new TextFieldWidget(font, guiLeft + 12, guiTop + 117, 26, 11, "");
        maxField.setMaxStringLength(3);
        maxField.setText(prevMax);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //TODO: Lang Keys
        drawString(TextComponentUtil.build(Translation.of("gui.digitalMinerConfig")), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.filters"), ":"), 11, 19, 0x00CD00);
        drawString(TextComponentUtil.build("T: " + tileEntity.filters.size()), 11, 28, 0x00CD00);
        drawString(TextComponentUtil.build("I: ", OnOff.of(tileEntity.inverse)), 11, 131, 0x00CD00);
        drawString(TextComponentUtil.build("Radi: " + tileEntity.getRadius()), 11, 58, 0x00CD00);
        drawString(TextComponentUtil.build("Min: " + tileEntity.minY), 11, 83, 0x00CD00);
        drawString(TextComponentUtil.build("Max: " + tileEntity.maxY), 11, 108, 0x00CD00);

        for (int i = 0; i < 4; i++) {
            IFilter filter = tileEntity.filters.get(getFilterIndex() + i);
            if (filter != null) {
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    renderItem(((IItemStackFilter) filter).getItemStack(), 59, yStart + 3);
                    drawString(TextComponentUtil.build(Translation.of("gui.itemFilter")), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IOreDictFilter) {
                    IOreDictFilter oreFilter = (IOreDictFilter) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(TextComponentUtil.build(Translation.of("gui.oredictFilter")), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IMaterialFilter) {
                    renderItem(((IMaterialFilter) filter).getMaterialItem(), 59, yStart + 3);
                    drawString(TextComponentUtil.build(Translation.of("gui.materialFilter")), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter modFilter = (IModIDFilter) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(TextComponentUtil.build(Translation.of("gui.modIDFilter")), 78, yStart + 2, 0x404040);
                }
            }
        }
        if (inverseButton.isMouseOver(mouseX, mouseY)) {
            displayTooltip(TextComponentUtil.build(Translation.of("mekanism.gui.digitalMiner.inverse")), mouseX - guiLeft, mouseY - guiTop);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        radiusField.drawTextBox();
        minField.drawTextBox();
        maxField.drawTextBox();
        MekanismRenderer.resetColor();
    }

    @Override
    public boolean charTyped(char c, int i) {
        if ((!radiusField.isFocused() && !minField.isFocused() && !maxField.isFocused()) || i == GLFW.GLFW_KEY_ESCAPE) {
            return super.charTyped(c, i);
        }
        if (i == GLFW.GLFW_KEY_ENTER) {
            if (radiusField.isFocused()) {
                setRadius();
                return true;
            } else if (minField.isFocused()) {
                setMinY();
                return true;
            } else if (maxField.isFocused()) {
                setMaxY();
                return true;
            }
        }
        if (Character.isDigit(c) || isTextboxKey(c, i)) {
            radiusField.charTyped(c, i);
            minField.charTyped(c, i);
            maxField.charTyped(c, i);
            return true;
        }
        return false;
    }

    private void setRadius() {
        if (!radiusField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(radiusField.getText()), MekanismConfigOld.current().general.digitalMinerMaxRadius.get()));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(6, toUse)));
            radiusField.setText("");
        }
    }

    private void setMinY() {
        if (!minField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(minField.getText()), tileEntity.maxY));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(7, toUse)));
            minField.setText("");
        }
    }

    private void setMaxY() {
        if (!maxField.getText().isEmpty()) {
            int toUse = Math.max(tileEntity.minY, Math.min(Integer.parseInt(maxField.getText()), 255));
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(8, toUse)));
            maxField.setText("");
        }
    }
}