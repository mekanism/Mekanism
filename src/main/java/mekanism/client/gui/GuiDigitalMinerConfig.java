package mekanism.client.gui;

import javax.annotation.Nullable;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.button.MekanismImageButton;
import mekanism.client.gui.button.TranslationButton;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IOreDictFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.inventory.container.tile.filter.list.DigitalMinerConfigContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class GuiDigitalMinerConfig extends GuiFilterHolder<MinerFilter, TileEntityDigitalMiner, DigitalMinerConfigContainer> {

    private TextFieldWidget radiusField;
    private TextFieldWidget minField;
    private TextFieldWidget maxField;

    public GuiDigitalMinerConfig(DigitalMinerConfigContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
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
        if (button == 0) {
            double xAxis = mouseX - guiLeft;
            double yAxis = mouseY - guiTop;

            if (xAxis >= 154 && xAxis <= 166 && yAxis >= getScroll() + 18 && yAxis <= getScroll() + 18 + 15) {
                if (needsScrollBars()) {
                    dragOffset = (int) (yAxis - (getScroll() + 18));
                    isDragging = true;
                } else {
                    scroll = 0;
                }
            }

            HashList<MinerFilter> filters = tileEntity.getFilters();
            //Check for filter interaction
            for (int i = 0; i < 4; i++) {
                int index = getFilterIndex() + i;
                MinerFilter filter = filters.get(index);
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
                        if (index < filters.size() - 1) {
                            if (xAxis >= arrowX && xAxis <= arrowX + 10 && yAxis >= yStart + 21 && yAxis <= yStart + 27) {
                                //Process down button click
                                sendDataFromClick(TileNetworkList.withContents(12, index));
                                return true;
                            }
                        }
                        if (filter instanceof IItemStackFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_ITEMSTACK, tileEntity.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IOreDictFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_TAG, tileEntity.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IMaterialFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MATERIAL, tileEntity.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        } else if (filter instanceof IModIDFilter) {
                            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_FILTER_MOD_ID, tileEntity.getPos(), index));
                            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "digital_miner_config.png");
    }

    @Override
    public void init() {
        super.init();
        addButton(new TranslationButton(this, guiLeft + filterX, guiTop + 136, filterW, 20, "gui.mekanism.newFilter",
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.DM_SELECT_FILTER_TYPE, tileEntity.getPos()))));
        addButton(new MekanismImageButton(this, guiLeft + 5, guiTop + 5, 11, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, tileEntity.getPos()))));
        addButton(new MekanismImageButton(this, guiLeft + 39, guiTop + 67, 11, 12, getButtonLocation("checkmark"), this::setRadius));
        addButton(new MekanismImageButton(this, guiLeft + 39, guiTop + 92, 11, 12, getButtonLocation("checkmark"), this::setMinY));
        addButton(new MekanismImageButton(this, guiLeft + 39, guiTop + 117, 11, 12, getButtonLocation("checkmark"), this::setMaxY));
        addButton(new MekanismImageButton(this, guiLeft + 11, guiTop + 141, 14, getButtonLocation("strict_input"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tileEntity, TileNetworkList.withContents(10))),
              getOnHover("gui.mekanism.digitalMiner.inverse")));

        String prevRad = radiusField != null ? radiusField.getText() : "";
        String prevMin = minField != null ? minField.getText() : "";
        String prevMax = maxField != null ? maxField.getText() : "";

        addButton(radiusField = new TextFieldWidget(font, guiLeft + 12, guiTop + 67, 26, 11, ""));
        radiusField.setMaxStringLength(Integer.toString(MekanismConfig.general.digitalMinerMaxRadius.get()).length());
        radiusField.setText(prevRad);

        addButton(minField = new TextFieldWidget(font, guiLeft + 12, guiTop + 92, 26, 11, ""));
        minField.setMaxStringLength(3);
        minField.setText(prevMin);

        addButton(maxField = new TextFieldWidget(font, guiLeft + 12, guiTop + 117, 26, 11, ""));
        maxField.setMaxStringLength(3);
        maxField.setText(prevMax);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        HashList<MinerFilter> filters = tileEntity.getFilters();
        //TODO: Lang Keys
        drawString(TextComponentUtil.translate("gui.mekanism.digitalMinerConfig"), 43, 6, 0x404040);
        drawString(TextComponentUtil.build(Translation.of("gui.mekanism.filters"), ":"), 11, 19, 0x00CD00);
        drawString(TextComponentUtil.build("T: " + filters.size()), 11, 28, 0x00CD00);
        drawString(TextComponentUtil.build("I: ", OnOff.of(tileEntity.inverse)), 11, 131, 0x00CD00);
        drawString(TextComponentUtil.build("Radi: " + tileEntity.getRadius()), 11, 58, 0x00CD00);
        drawString(TextComponentUtil.build("Min: " + tileEntity.minY), 11, 83, 0x00CD00);
        drawString(TextComponentUtil.build("Max: " + tileEntity.maxY), 11, 108, 0x00CD00);

        for (int i = 0; i < 4; i++) {
            MinerFilter filter = filters.get(getFilterIndex() + i);
            if (filter != null) {
                int yStart = i * filterH + filterY;
                if (filter instanceof IItemStackFilter) {
                    renderItem(((IItemStackFilter) filter).getItemStack(), 59, yStart + 3);
                    drawString(TextComponentUtil.translate("gui.mekanism.itemFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IOreDictFilter) {
                    IOreDictFilter oreFilter = (IOreDictFilter) filter;
                    if (!oreDictStacks.containsKey(oreFilter)) {
                        updateStackList(oreFilter);
                    }
                    renderItem(oreDictStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(TextComponentUtil.translate("gui.mekanism.oredictFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IMaterialFilter) {
                    renderItem(((IMaterialFilter) filter).getMaterialItem(), 59, yStart + 3);
                    drawString(TextComponentUtil.translate("gui.mekanism.materialFilter"), 78, yStart + 2, 0x404040);
                } else if (filter instanceof IModIDFilter) {
                    IModIDFilter modFilter = (IModIDFilter) filter;
                    if (!modIDStacks.containsKey(modFilter)) {
                        updateStackList(modFilter);
                    }
                    renderItem(modIDStacks.get(filter).renderStack, 59, yStart + 3);
                    drawString(TextComponentUtil.translate("gui.mekanism.modIDFilter"), 78, yStart + 2, 0x404040);
                }
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                //Manually handle hitting escape making the field lose focus
                focusedField.setFocused2(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                if (radiusField.isFocused()) {
                    setRadius();
                } else if (minField.isFocused()) {
                    setMinY();
                } else if (maxField.isFocused()) {
                    setMaxY();
                }
                return true;
            }
            return focusedField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        TextFieldWidget focusedField = getFocusedField();
        if (focusedField != null) {
            if (Character.isDigit(c)) {
                return focusedField.charTyped(c, keyCode);
            }
            return false;
        }
        return super.charTyped(c, keyCode);
    }

    @Nullable
    private TextFieldWidget getFocusedField() {
        if (radiusField.isFocused()) {
            return radiusField;
        } else if (minField.isFocused()) {
            return minField;
        } else if (maxField.isFocused()) {
            return maxField;
        }
        return null;
    }

    private void setRadius() {
        if (!radiusField.getText().isEmpty()) {
            int toUse = Math.max(0, Math.min(Integer.parseInt(radiusField.getText()), MekanismConfig.general.digitalMinerMaxRadius.get()));
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